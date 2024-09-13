package com.example.timeroutetracker.components

import android.text.TextPaint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import com.example.timeroutetracker.utils.linearRatio

private val colors = listOf(
  Color.Black,
  Color.White,
  Color.Red,
  Color.Gray,
  Color.Green,
  Color.Blue,
  Color.Yellow,
  Color.Magenta,
  Color.Cyan
).map { it.toArgb() }

fun getHighContrastColor(backgroundColor: Int?): Int? {
  return backgroundColor?.let { bc -> colors.maxByOrNull { ColorUtils.calculateContrast(bc, it) } }
}


@Composable
fun BarChart(
  data: List<List<Pair<Float, Color>>>,  // 每个柱体的数据（成分占比及颜色）
  modifier: Modifier = Modifier,
  xAxisBegin: Int = 0,  // 横坐标起点
  xAxisStep: Int? = null,  // 横坐标间隔
  onBarClick: (Int) -> Unit,  // 每个柱体的点击处理
) {
  val density = LocalDensity.current // 获取当前的 Density 实例
  val pxToDp = { px: Int -> with(density) { px.toDp() } }
  val dpToSp = { dp: Dp -> with(density) { dp.toSp() } }

  val summedHeight = data.map { it.map { it -> it.first }.sum() } // 获取每个柱体的总高度
  val maxHeight = summedHeight.maxOrNull() ?: 1f  // 获取最大高度来进行比例缩放

  // 自动计算柱体宽度和间距
  val minBarWidth = 10.dp
  val maxBarWidth = 80.dp
  val minimalInterval = 5.dp
  val expectedMaxPadding = 100.dp

  // Row 的总宽度
  val temp = LocalContext.current.resources.displayMetrics.widthPixels
  var widthDp by remember { mutableStateOf(pxToDp(temp)) }
  // 滑动 state
  val scrollState = rememberScrollState()
  val defaultTextColor = MaterialTheme.colorScheme.onSurface

  Row(
    modifier = modifier
      .horizontalScroll(scrollState)
      .fillMaxWidth()
      .onGloballyPositioned { coordinates ->
        // 获取 Row 的宽度
        widthDp = pxToDp(coordinates.size.width)
      }
  ) {
    val eachWidth = widthDp / (data.size)
    val (barWidth, padding) = linearRatio(
      aMin = minBarWidth,
      aMax = maxBarWidth,
      bMin = minimalInterval,
      bExpectedMax = expectedMaxPadding,
      x = eachWidth
    )
    data.forEachIndexed { index, barData ->
      // 所有柱体填充满 Row 区域，并添加 padding
      Box(
        modifier = Modifier
          .padding(horizontal = padding / 2)
          .requiredWidthIn(min = barWidth)
          .weight(1f)
          .clickable { onBarClick(index) }
      ) {
        Canvas(
          modifier = Modifier
            .fillMaxSize()
            .padding(bottom = barWidth)
        ) {
          var accumulatedHeight = 0f
          val cornerRadiusPx = 8.dp.toPx()

          barData.forEachIndexed { i, (value, color) ->
            val proportion = value / maxHeight
            val barHeight = size.height * proportion

            drawRoundRect(
              color = color,
              topLeft = Offset(x = 0f, y = size.height - accumulatedHeight - barHeight),
              size = size.copy(height = barHeight),
              cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
            )
            accumulatedHeight += barHeight
          }

          // 画柱形图的顶部数据（柱体高度）
          val lastColor = barData.lastOrNull()?.second
          val textPaint = TextPaint().apply {
            textSize = 8.sp.toPx()
            color = getHighContrastColor(lastColor?.toArgb()) ?: defaultTextColor.toArgb()
          }
          // 绘制文本
          drawIntoCanvas { canvas ->
            // 计算文本位置
            val text = "%.2f".format(summedHeight[index])
            val textWidth = textPaint.measureText(text)
            val textHeight = textPaint.fontMetrics.run { ascent - descent }

            val textX = (size.width - textWidth) / 2
            val textY = size.height - accumulatedHeight - textHeight
            canvas.nativeCanvas.drawText(text, textX, textY, textPaint)
          }
        }
        // 显示横坐标
        if (index % (xAxisStep ?: (data.size / 5)) == xAxisBegin) {
          Text(
            text = "$index",
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .padding(bottom = 2.dp),
            textAlign = TextAlign.Center,
            color = defaultTextColor,
            fontSize = dpToSp(barWidth / 2)
          )
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun ExampleBarChart() {
  // 示例数据：每个柱体由多个成分组成（占比和颜色）
  val data = listOf(
    listOf(0.4f to Color.Red, 0.1f to Color.Blue),
    listOf(0.2f to Color.Green, 0.4f to Color.Yellow),
    listOf(0.3f to Color.Green, 0.3f to Color.Yellow),
    listOf(0.3f to Color.Green, 0.2f to Color.Yellow),
    listOf(0.3f to Color.Green, 0.5f to Color.Yellow),
    listOf(0.15f to Color.Green, 0.4f to Color.Yellow),
    listOf(0.4f to Color.Magenta, 0.5f to Color.Cyan)
  )

  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    BarChart(
      data = data,
      modifier = Modifier
        .fillMaxSize()
        .padding(start = 0.dp, top = 10.dp, end = 0.dp, bottom = 10.dp),
      xAxisBegin = 1,
      xAxisStep = 3
    ) { index ->
      Log.i("Test", "Bar $index clicked")
    }
  }
}