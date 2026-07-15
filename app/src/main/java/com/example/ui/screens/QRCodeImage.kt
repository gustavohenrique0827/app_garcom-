package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.Hashtable

@Composable
fun QRCodeImage(
    text: String,
    modifier: Modifier = Modifier,
    qrColor: Color = Color.Black,
    backgroundColor: Color = Color.White
) {
    val size = 512
    val bitMatrix = remember(text) {
        try {
            val hints = Hashtable<EncodeHintType, Any>().apply {
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M)
                put(EncodeHintType.MARGIN, 1)
            }
            MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size, hints)
        } catch (e: Exception) {
            null
        }
    }

    if (bitMatrix != null) {
        Canvas(modifier = modifier) {
            val matrixWidth = bitMatrix.width
            val matrixHeight = bitMatrix.height
            val pixelWidth = this.size.width / matrixWidth
            val pixelHeight = this.size.height / matrixHeight

            // Draw background
            drawRect(color = backgroundColor, size = this.size)

            for (y in 0 until matrixHeight) {
                for (x in 0 until matrixWidth) {
                    if (bitMatrix.get(x, y)) {
                        drawRect(
                            color = qrColor,
                            topLeft = Offset(x * pixelWidth, y * pixelHeight),
                            size = Size(pixelWidth + 0.5f, pixelHeight + 0.5f)
                        )
                    }
                }
            }
        }
    }
}
