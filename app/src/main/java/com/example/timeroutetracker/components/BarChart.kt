package com.example.timeroutetracker.components

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.timeroutetracker.utils.linearRatio

class BarChart {
}


@Composable
fun BarChart(
  data: List<List<Pair<Float, Color>>>,  // 每个柱体的数据（成分占比及颜色）
  modifier: Modifier = Modifier,
  onBarClick: (Int) -> Unit  // 每个柱体的点击处理
) {
  val density = LocalDensity.current // 获取当前的 Density 实例
  val pxToDp = { px: Int -> with(density) { px.toDp() } }

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
        Canvas(modifier = Modifier.fillMaxSize()) {
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
    listOf(0.3f to Color.Green, 0.7f to Color.Yellow),
    listOf(0.3f to Color.Green, 0.7f to Color.Yellow),
    listOf(0.3f to Color.Green, 0.7f to Color.Yellow),
    listOf(0.3f to Color.Green, 0.7f to Color.Yellow),
    listOf(0.3f to Color.Green, 0.7f to Color.Yellow),
    listOf(0.5f to Color.Magenta, 0.5f to Color.Cyan)
  )

  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    BarChart(
      data = data,
      modifier = Modifier
        .fillMaxSize()
        .padding(start = 0.dp, top = 10.dp, end = 0.dp, bottom = 10.dp)
    ) { index ->
      Log.i("Test", "Bar $index clicked")
    }
  }
}