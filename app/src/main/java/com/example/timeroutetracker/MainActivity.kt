package com.example.timeroutetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.timeroutetracker.ui.theme.TimeRouteTrackerTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      TimeRouteTrackerTheme {
        MyApp()
      }
    }
  }
}

@Preview(showBackground = true, name = "App")
@Composable
fun MyApp() {
  var selectedTab by remember { mutableStateOf(0) } // 当前选中的按钮索引

  Scaffold(
    bottomBar = {
      BottomNavigationBar(selectedTab) { newIndex ->
        selectedTab = newIndex
      }
    }
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      when (selectedTab) {
        0 -> ScreenContent("Time")
        1 -> ScreenContent("Route")
        2 -> ScreenContent("Settings")
      }
    }
  }
}

@Composable
fun ScreenContent(text: String) {
  Text(
    text = text,
    fontSize = 24.sp,
    textAlign = TextAlign.Center,
    modifier = Modifier.fillMaxSize()
  )
}

@Composable
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
  val items = listOf(
    NavItem("Time", Icons.Default.Schedule),
    NavItem("Location", Icons.Default.Route),
    NavItem("Settings", Icons.Default.Settings),
  )

  NavigationBar(
    tonalElevation = 8.dp // 提升材质感
  ) {
    items.forEachIndexed { index, item ->
      val isSelected = index == selectedTab

      NavigationBarItem(
        icon = {
          Icon(
            imageVector = item.icon,
            contentDescription = item.label
          )
        },
        label = { Text(item.label) },
        selected = isSelected,
        onClick = { onTabSelected(index) },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = Color.White,
          selectedTextColor = MaterialTheme.colorScheme.primary,
          unselectedIconColor = Color.Gray,
          unselectedTextColor = Color.Gray,
          indicatorColor = MaterialTheme.colorScheme.primary
        ),
        alwaysShowLabel = true
      )
    }
  }
}

data class NavItem(val label: String, val icon: ImageVector)