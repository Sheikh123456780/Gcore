package android.app

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import java.lang.reflect.Field
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object AdvancedPopupHelper {

    private val handler = Handler(Looper.getMainLooper())

    /* ================= GET TOP ACTIVITY (FIXED) ================= */
    private fun getTopActivity(): Activity? {
        return try {
            val atClass = Class.forName("android.app.ActivityThread")
            val currentAT = atClass.getMethod("currentActivityThread").invoke(null)
            val mActivitiesField: Field = atClass.getDeclaredField("mActivities")
            mActivitiesField.isAccessible = true
            val activities = mActivitiesField.get(currentAT) as Map<*, *>

            for (record in activities.values) {
                val rClass = record!!::class.java
                val pausedField = rClass.getDeclaredField("paused")
                pausedField.isAccessible = true
                if (!pausedField.getBoolean(record)) {
                    val activityField = rClass.getDeclaredField("activity")
                    activityField.isAccessible = true
                    return activityField.get(record) as Activity
                }
            }
            null
        } catch (_: Throwable) {
            null
        }
    }

    /* ================= ENTRY ================= */
    @JvmStatic
    fun showAuto() {
        val act = getTopActivity() ?: return
        if (act.isFinishing || act.isDestroyed) return
        showPopup(act)
    }

    /* ================= SHOW POPUP ================= */
    private fun showPopup(act: Activity) {
        handler.post {
            try {
                val dialog = Dialog(act, android.R.style.Theme_Translucent_NoTitleBar)
                dialog.setCancelable(false)

                val webView = WebView(act)
                webView.settings.javaScriptEnabled = true
                webView.settings.domStorageEnabled = true
                webView.setBackgroundColor(Color.TRANSPARENT)
                webView.webViewClient = WebViewClient()

                /* 🔹 JS → Android bridge (auto close) */
                webView.addJavascriptInterface(object {
                    @JavascriptInterface
                    fun close() {
                        handler.post {
                            if (dialog.isShowing) dialog.dismiss()
                        }
                    }
                }, "Android")

                webView.loadDataWithBaseURL(
                    null,
                    HTML,
                    "text/html",
                    "utf-8",
                    null
                )

                dialog.setContentView(webView)
                dialog.show()

                /* 🔹 EXACT SIZE (same as your UI) */
                dialog.window?.apply {
                    setLayout(dp(act, 260), dp(act, 340))
                    setGravity(Gravity.CENTER)
                    addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                    setDimAmount(0.6f)
                }

            } catch (_: Throwable) {
            }
        }
    }

    /* ================= DP UTILS ================= */
    private fun dp(act: Activity, v: Int): Int {
        return (v * act.resources.displayMetrics.density).toInt()
    }

    /* ================= FULL HTML (UNCHANGED UI + AUTO CLOSE) ================= */
    private const val HTML = """
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<script src="https://unpkg.com/lottie-web@5.12.2/build/player/lottie.min.js"></script>

<style>
*{margin:0;padding:0;box-sizing:border-box;font-family:system-ui,'Segoe UI',sans-serif}
body{background:transparent;display:flex;justify-content:center;align-items:center;height:100vh}

.card{
    width:230px;
    background:#111;
    border-radius:15px;
    padding:11px;
    border:1.2px solid #ff3b3b;
    box-shadow:0 14px 32px rgba(0,0,0,.6);
}

.title-row{display:flex;align-items:center;justify-content:center;gap:6px;margin-bottom:6px}
.title-icon{width:18px;height:18px}
.title-text{font-size:12px;font-weight:600;color:#fff;white-space:nowrap}

.row{display:flex;align-items:center;gap:6px;margin:3px 0}
.icon{width:16px;height:16px}

.letter{display:inline-flex;white-space:pre}
.letter span{display:inline-block;animation:letterZoom 3s infinite}
.space{width:4px}

@keyframes letterZoom{
    0%{transform:scale(1)}
    20%{transform:scale(1.25)}
    40%{transform:scale(1)}
    100%{transform:scale(1)}
}

.color-status{color:#fff;font-size:10.5px}
.color-dev{color:#4cff4c;font-size:10.5px}
.color-fix{color:#4cbcff;font-size:10.5px}
.color-contact{color:#ffd24c;font-size:10.5px}

.footer{margin-top:5px;text-align:center}
.footer-row{display:flex;align-items:center;justify-content:center;gap:4px}
.footer-icon{width:12px;height:12px}
.footer-text{font-size:10.5px;font-weight:600;color:#ffd24c}
.footer-premium{font-size:9.5px;color:#fff}
</style>
</head>

<body>
<div class="card">

<div class="title-row">
<div id="iconTitle" class="title-icon"></div>
<div class="title-text letter">ACCESS EXPIRED</div>
</div>

<div class="row">
<div id="iconStatus" class="icon"></div>
<span class="letter color-status">Support A10 to A16</span>
</div>

<div class="row">
<div id="iconDev" class="icon"></div>
<span class="letter color-dev">Dev : VBoxCore</span>
</div>

<div class="row">
<div id="iconFix" class="icon"></div>
<span class="letter color-fix">UI & Fix : Any issue</span>
</div>

<div class="row">
<div id="iconContact" class="icon"></div>
<span class="letter color-contact">Contact : Telegram / WhatsApp</span>
</div>

<div class="footer">
<div class="footer-row">
<div id="iconPerfect" class="footer-icon"></div>
<span class="footer-text letter">Perfect</span>
<span class="footer-premium letter">VBox Premium</span>
</div>
</div>

</div>

<script>
lottie.loadAnimation({container:iconTitle,renderer:'svg',loop:true,autoplay:true,
path:'https://assets10.lottiefiles.com/packages/lf20_j1adxtyb.json'});
lottie.loadAnimation({container:iconPerfect,renderer:'svg',loop:true,autoplay:true,
path:'https://assets4.lottiefiles.com/packages/lf20_jtbfg2nb.json'});
lottie.loadAnimation({container:iconStatus,renderer:'svg',loop:true,autoplay:true,
path:'https://assets2.lottiefiles.com/packages/lf20_jcikwtux.json'});
lottie.loadAnimation({container:iconDev,renderer:'svg',loop:true,autoplay:true,
path:'https://assets7.lottiefiles.com/packages/lf20_w51pcehl.json'});
lottie.loadAnimation({container:iconFix,renderer:'svg',loop:true,autoplay:true,
path:'https://assets6.lottiefiles.com/packages/lf20_t9gkkhz4.json'});
lottie.loadAnimation({container:iconContact,renderer:'svg',loop:true,autoplay:true,
path:'https://assets3.lottiefiles.com/packages/lf20_5ngs2ksb.json'});

document.querySelectorAll('.letter').forEach(el=>{
    const t = el.textContent;
    el.textContent='';
    [...t].forEach((c,i)=>{
        if(c===' '){
            const s=document.createElement('span');
            s.className='space';el.appendChild(s)
        }else{
            const sp=document.createElement('span');
            sp.textContent=c;
            sp.style.animationDelay=(i*0.15)+'s';
            el.appendChild(sp)
        }
    })
});

/* ✅ AUTO CLOSE AFTER 5 SECONDS */
setTimeout(function(){
    if (window.Android) Android.close();
}, 10000);
</script>

</body>
</html>
"""
}