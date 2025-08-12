package com.example.remember.ui.pdf_viewer

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.navigation.NavController
import java.io.File
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.input.pointer.positionChange
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    parentNavController: NavController,
    pdfUri: String
) {
    val renderer = remember(pdfUri) {
        try {
            val file = File(pdfUri.toUri().path!!)
            val input = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            PdfRenderer(input)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    DisposableEffect(key1 = renderer) {
        onDispose {
            renderer?.close()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PDF Viewer") },
                navigationIcon = {
                    IconButton(onClick = { parentNavController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (renderer == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Failed to load PDF.")
            }
        } else {
            HorizontalPager(
                state = rememberPagerState { renderer.pageCount },
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) { pageIndex ->
                val page = renderer.openPage(pageIndex)
                val bitmap = createBitmap(page.width, page.height)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                ZoomableBitmapImage(bitmap = bitmap)
            }
        }
    }
}


@Composable
fun ZoomableBitmapImage(
    bitmap: Bitmap,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                awaitEachGesture {
                        awaitFirstDown()
                        do {
                            val event = awaitPointerEvent()
                            val zoom = event.calculateZoom()
                            scale = (scale * zoom).coerceIn(1f, 5f)

                            if (scale > 1f) {
                                val pan = event.calculatePan()
                                val newOffset = offset + pan
                                val maxOffsetX = (size.width * (scale - 1)) / 2
                                val maxOffsetY = (size.height * (scale - 1)) / 2
                                offset = Offset(
                                    x = newOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
                                    y = newOffset.y.coerceIn(-maxOffsetY, maxOffsetY)
                                )
                                if (pan != Offset.Zero) {
                                    event.changes.forEach {
                                        if (it.positionChange() != Offset.Zero) it.consume()
                                    }
                                }
                            } else {
                                offset = Offset.Zero
                            }
                        } while (event.changes.any { it.pressed })
                    }

            }
    ) {
        Image(
            painter = BitmapPainter(bitmap.asImageBitmap()),
            contentDescription = "Zoomable PDF Page",
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentScale = ContentScale.Fit
        )
    }
}