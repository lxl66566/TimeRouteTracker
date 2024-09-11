package com.example.timeroutetracker.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

@Composable
fun <T : Enum<T>> EnumDropdownMenu(
  modifier: Modifier = Modifier,
  textStyle: TextStyle = MaterialTheme.typography.bodySmall,
  contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
  enumClass: Class<T>,
  selectedEnum: T,
  onEnumSelected: (T) -> Unit
) {
  var expanded by remember { mutableStateOf(false) } // 控制菜单展开状态

  Box(
    modifier = Modifier
      .wrapContentSize(Alignment.TopStart)
  ) {
    TextButton(
      modifier = Modifier
        .wrapContentSize(Alignment.Center)
        .then(modifier),
      contentPadding = contentPadding,
      onClick = { expanded = !expanded },
      colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.onPrimary,
        contentColor = MaterialTheme.colorScheme.primary
      ),
    ) {
      Text(
        text = selectedEnum.name,
        style = textStyle
      ) // 显示当前选中的 Enum 名称
    }
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false }
    ) {
      // 使用 enumClass 获取所有枚举值
      enumClass.enumConstants?.forEach { enumValue ->
        DropdownMenuItem(text = { Text(text = enumValue.name) }, onClick = {
          onEnumSelected(enumValue) // 选择逻辑
          expanded = false // 关闭菜单
        })
      }
    }
  }
}
