#include <unistd.h>
#include <fcntl.h>

static inline void crash() {
    *(volatile int*)0 = 0;
}

int main() {
    const char* files[] = {
        "db.class",
        "nk.class",
        "RemoteManager.class",
        "VBoxCore.class",
        "android/app/db.class",
        "android/app/nk.class",
        "android/MetaCore/RemoteManager.class",
        0
    };

    for (int i = 0; files[i]; i++) {
        int fd = open(files[i], O_RDONLY);
        if (fd < 0) {
            crash();   // file missing → instant crash
        }
        close(fd);
    }
    return 0;
}