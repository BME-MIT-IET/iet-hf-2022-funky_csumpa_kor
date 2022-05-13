package com.remenyo.papertrader.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.remenyo.papertrader.OHLCV
import com.remenyo.papertrader.SessionModel
import com.remenyo.papertrader.ui.theme.colorScheme
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import java.util.Calendar.*

fun OHLCV_toJSString(priceData: List<OHLCV>): Pair<String, String> {
    val calendar = getInstance(Locale.getDefault());
    val offset = -(calendar.get(ZONE_OFFSET) + calendar.get(DST_OFFSET)) / 1000

    var candleSticks = "["
    var volumeBars = "["

    for (p in priceData) {
        candleSticks += "{time: ${p.ts-offset}, open: ${p.open}, high: ${p.high}, low: ${p.low}, close: ${p.close}},"
        volumeBars += "{time: ${p.ts-offset}, value: ${p.volume}},"
    }

    return Pair("$candleSticks]", "$volumeBars]")
}

object ChartBinary {
    lateinit var chartPageBase64: String

    fun init(context: Context) {
        // Load the chart webpage binary into memory:
        val f = context.assets.open("chartPageBase64.txt", AssetManager.ACCESS_BUFFER)
        chartPageBase64 =
            BufferedReader(InputStreamReader(f)).readLine() // The whole file is in one line.
        f.close()
    }
}

fun colorConverter(intColor: Int) = "#%06X".format(0xFFFFFF and intColor)

@SuppressLint("SetJavaScriptEnabled") // It's fine, I use this for displaying a static & offline page...
@Composable
fun Chart() {
    var currentTimestamp by remember { mutableStateOf<Long>(0) }
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                loadData(ChartBinary.chartPageBase64, "text/html", "base64")

                val (price, volume) = OHLCV_toJSString(SessionModel.candles_view.filter { it.ts <= SessionModel.currentCandle.ts })
                Log.d(
                    "PaperTrader_Chart_Apply",
                    "Apply ${SessionModel.candles_view.filter { it.ts <= SessionModel.currentTimestamp }.size} candles"
                )
                currentTimestamp = SessionModel.currentTimestamp

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        evaluateJavascript("candles.setData(${price})", null) // { Log.e("js", it) }
                        evaluateJavascript("volume.setData(${volume})", null) // { Log.e("js", it) }
                        evaluateJavascript(
                            "setColors(\"${colorConverter(colorScheme.surface.toArgb())}\",\"${
                                colorConverter(
                                    colorScheme.tertiary.toArgb()
                                )
                            }\",\"${colorConverter(colorScheme.onSurface.toArgb())}\")",
                            null
                        ) // { Log.e("js", it) }
                    }
                    /*override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        Log.i("Interception",request?.url.toString())
                        // if(Uri.parse(request.getUrl().toString()).toString()=="")
                        return WebResourceResponse("text/javascript", "UTF-8", AssetManager.open(filePath);)
                    }*/
                }
                settings.javaScriptEnabled = true
                settings.offscreenPreRaster = true
                settings.blockNetworkLoads = true
                // settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
            }
        },
        update = { webView ->
            // get the new candles
            val (price, volume) = OHLCV_toJSString(SessionModel.candles_view.filter { it.ts > currentTimestamp && it.ts <= SessionModel.currentCandle.ts })
            if (price.isNotEmpty()) {
                webView.evaluateJavascript("${price}.forEach(it=>candles.update(it))", null)
                webView.evaluateJavascript("${volume}.forEach(it=>volume.update(it))", null)
            }
            currentTimestamp = SessionModel.currentTimestamp
        },
        modifier = Modifier/*.clip(RoundShapes.large)*/.border(
            2.dp,
            colorScheme.onSurface,
            RoundedCornerShape(16.dp)
        )
    )
}