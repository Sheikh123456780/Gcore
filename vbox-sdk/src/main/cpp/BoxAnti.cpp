#include <dlfcn.h>
#include <unistd.h>
#include <sys/mman.h>
#include <android/log.h>
#include <fcntl.h>
#include <dirent.h>
#include <pthread.h>
#include <time.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <errno.h>
#include <sys/stat.h>
#include <sys/syscall.h>
#include <sys/wait.h>
#include <sys/prctl.h>
#include <jni.h>
#include <ctype.h>
#include <stdarg.h>   // 🔥 ADD

// ==================== SIMPLE CONFIG ====================
#define LOG_TAG "GAME_FIX"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define CHECK_INTERVAL 5

// ==================== GLOBAL STATE ====================
static volatile bool engine_running = true;
static time_t app_start_time = 0;
static pthread_t fix_thread;
static char current_package[256] = {0};

// ==================== ORIGINAL FUNCTIONS ====================
typedef void* (*dlopen_fn)(const char*, int);
typedef int (*open_fn)(const char*, int, ...);
typedef ssize_t (*read_fn)(int, void*, size_t);
typedef int (*access_fn)(const char*, int);
typedef int (*openat_fn)(int, const char*, int, ...);   // 🔥 ADD

static dlopen_fn real_dlopen = NULL;
static open_fn real_open = NULL;
static read_fn real_read = NULL;
static access_fn real_access = NULL;
static openat_fn real_openat = NULL;                    // 🔥 ADD

// ==================== PACKAGE DETECTION ====================
void detect_package() {
    FILE* cmdline = fopen("/proc/self/cmdline", "r");
    if (cmdline) {
        if (fgets(current_package, sizeof(current_package), cmdline)) {
            LOGD("Package: %s", current_package);
        }
        fclose(cmdline);
    }

    if (strlen(current_package) == 0) {
        char path[256];
        snprintf(path, sizeof(path), "/proc/%d/cmdline", getppid());
        FILE* parent = fopen(path, "r");
        if (parent) {
            fgets(current_package, sizeof(current_package), parent);
            fclose(parent);
        }
    }
}

// ==================== SAFE SYSTEM CALL ====================
int safe_system(const char* cmd) {
    pid_t pid = fork();
    if (pid == 0) {
        char *args[] = {"sh", "-c", (char*)cmd, NULL};
        execvp("sh", args);
        _exit(1);
    } else if (pid > 0) {
        int status;
        waitpid(pid, &status, 0);
        return WEXITSTATUS(status);
    }
    return -1;
}

// ==================== 🔥 CRASH FIX PATH FILTER (ADD) ====================
static bool block_path(const char* path) {
    if (!path) return false;
    if (strstr(path, "/sys/devices/system/cpu")) return true;   // MAIN FIX
    if (strstr(path, "crashpad")) return true;
    return false;
}

// ==================== SIMPLE DLOPEN HOOK ====================
void* fix_dlopen(const char* name, int flags) {
    if (!real_dlopen) {
        void* libc = dlopen("libc.so", RTLD_LAZY);
        real_dlopen = (dlopen_fn)dlsym(libc, "dlopen");
        if (!real_dlopen) return NULL;
    }

    if (!name) return real_dlopen(name, flags);

    if (strlen(current_package) == 0) detect_package();

    if (strstr(name, ".so")) {
        if (strstr(name, current_package)) {
            if (!strstr(name, "/system/") &&
                !strstr(name, "/vendor/") &&
                !strstr(name, "/apex/") &&
                !strstr(name, "/data/app/")) {
                LOGD("Blocking dlopen: %s", name);
                return NULL;
            }
        }
    }
    return real_dlopen(name, flags);
}

// ==================== SIMPLE OPEN HOOK ====================
int fix_open(const char* path, int flags, ...) {
    mode_t mode = 0;
    if (flags & O_CREAT) {
        va_list args;
        va_start(args, flags);
        mode = va_arg(args, mode_t);
        va_end(args);
    }

    if (!real_open) {
        void* libc = dlopen("libc.so", RTLD_LAZY);
        real_open = (open_fn)dlsym(libc, "open");
        if (!real_open) return -1;
    }

    if (!path) return real_open(path, flags, mode);

    if (block_path(path)) {                      // 🔥 ADD
        LOGD("Blocked open: %s", path);
        errno = EACCES;
        return -1;
    }

    if (strlen(current_package) == 0) detect_package();

    if (strstr(path, current_package)) {
        const char* hidden_ext[] = {".so", ".cfg", ".ini", ".json", ".dex", NULL};
        const char* hidden_dirs[] = {"/lib/", "/files/lib/", "/cache/lib/", NULL};

        for (int i = 0; hidden_ext[i]; i++) {
            if (strstr(path, hidden_ext[i])) {
                for (int j = 0; hidden_dirs[j]; j++) {
                    if (strstr(path, hidden_dirs[j])) {
                        LOGD("Block open: %s", path);
                        errno = ENOENT;
                        return -1;
                    }
                }
            }
        }
    }
    return real_open(path, flags, mode);
}

// ==================== 🔥 OPENAT HOOK (ADD) ====================
int fix_openat(int dirfd, const char* path, int flags, ...) {
    mode_t mode = 0;
    if (flags & O_CREAT) {
        va_list args;
        va_start(args, flags);
        mode = va_arg(args, mode_t);
        va_end(args);
    }

    if (!real_openat) {
        void* libc = dlopen("libc.so", RTLD_LAZY);
        real_openat = (openat_fn)dlsym(libc, "openat");
        if (!real_openat) return -1;
    }

    if (block_path(path)) {
        LOGD("Blocked openat: %s", path);
        errno = EACCES;
        return -1;
    }

    return real_openat(dirfd, path, flags, mode);
}

// ==================== 🔥 ACCESS HOOK (ADD) ====================
int fix_access(const char* path, int mode) {
    if (block_path(path)) {
        LOGD("Blocked access: %s", path);
        errno = EACCES;
        return -1;
    }

    if (!real_access) {
        void* libc = dlopen("libc.so", RTLD_LAZY);
        real_access = (access_fn)dlsym(libc, "access");
        if (!real_access) return -1;
    }
    return real_access(path, mode);
}

// ==================== MEMORY PROTECTION ====================
void protect_memory() {
    void* addr = mmap(NULL, 4096, PROT_READ, MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
    if (addr != MAP_FAILED) munmap(addr, 4096);
    safe_system("sync");
}

// ==================== CRASH PREVENTION ====================
void prevent_crash() {
    time_t now = time(NULL);
    int seconds = now - app_start_time;
    static int last_fix_time = 0;

    if (seconds > 30 && (seconds - last_fix_time > 60)) {
        int crash_times[] = {120, 180, 240, 300};
        for (int i = 0; i < 4; i++) {
            if (seconds >= crash_times[i] - 10 &&
                seconds <= crash_times[i] + 10) {
                LOGD("Preventing crash at %d seconds", seconds);
                protect_memory();
                usleep(10000 + (rand() % 40000));
                last_fix_time = seconds;
                break;
            }
        }
    }
}

// ==================== ANTI-BAN ====================
void anti_ban_check() {
    static time_t last_check = 0;
    time_t now = time(NULL);
    if (now - last_check < 30) return;
    last_check = now;

    FILE* status = fopen("/proc/self/status", "r");
    if (status) {
        char line[256];
        while (fgets(line, sizeof(line), status)) {
            if (strstr(line, "TracerPid:")) {
                int pid;
                if (sscanf(line, "TracerPid: %d", &pid) == 1 && pid > 0) {
                    LOGD("Debugger detected");
                }
            }
        }
        fclose(status);
    }

    if (rand() % 10 == 0) {
        app_start_time = now;
        LOGD("Timer reset");
    }
}

// ==================== MONITOR THREAD ====================
void* monitor_func(void* arg) {
    LOGD("Monitor started");
    sleep(15);
    while (engine_running) {
        prevent_crash();
        anti_ban_check();
        sleep(CHECK_INTERVAL + (rand() % 4));
    }
    return NULL;
}

// ==================== SETUP ====================
void setup_hooks() {
    LOGD("Setting up hooks...");

    void* libc = dlopen("libc.so", RTLD_LAZY);
    if (libc) {
        real_dlopen = (dlopen_fn)dlsym(libc, "dlopen");
        real_open   = (open_fn)dlsym(libc, "open");
        real_openat = (openat_fn)dlsym(libc, "openat"); // 🔥 ADD
        real_access = (access_fn)dlsym(libc, "access"); // 🔥 ADD
        dlclose(libc);
    }

    detect_package();

    pthread_create(&fix_thread, NULL, monitor_func, NULL);
    pthread_detach(fix_thread);

    prctl(PR_SET_DUMPABLE, 0);
    LOGD("Hooks ready for: %s", current_package);
}

// ==================== ENTRY POINT ====================
__attribute__((constructor))
void init() {
    LOGD("Initializing...");
    sleep(3 + (rand() % 5));

    // 🔥 EXTRA SAFETY
    setenv("CHROMIUM_DISABLE_CRASHPAD", "1", 1);
    setenv("CHROME_DISABLE_CRASHPAD", "1", 1);

    app_start_time = time(NULL);
    srand(app_start_time);

    setup_hooks();
    LOGD("Initialization complete");
}

// ==================== CLEANUP ====================
__attribute__((destructor))
void cleanup() {
    LOGD("Cleaning up...");
    engine_running = false;
    sleep(1);
    LOGD("Cleanup complete");
}