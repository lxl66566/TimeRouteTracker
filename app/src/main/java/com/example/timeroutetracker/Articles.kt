package com.example.timeroutetracker

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter


// 定义文章数据类
data class Article(
  val id: Int,
  val title: String,
  val content: String,
  val imageUrl: String?
)


@Composable
fun ArticleListScreen(
  articles: List<Article>,
  articlesState: MutableList<Boolean>,
  onArticleClick: (Article) -> Unit
) {
  // 使用 LazyColumn 创建一个可滚动的列表
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    items(articles.zip(articlesState)) { (article, state) ->
      ArticleCard(article = article, state = state, onClick = { onArticleClick(article) })
    }
  }
}

@Composable
fun ArticleCard(article: Article, state: Boolean, onClick: () -> Unit) {
  // 使用 Card 和 Modifier 创建文章项
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp)
      .clickable { onClick() },
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // 如果有图片，显示图片
      if (!state) {
        article.imageUrl?.let { imageUrl ->
          Image(
            painter = rememberAsyncImagePainter(model = imageUrl),
            contentDescription = null,
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp)
              .clip(RoundedCornerShape(8.dp))
          )
          Spacer(modifier = Modifier.height(8.dp))
        }
      }

      // 显示标题
      Text(
        text = article.title,
        style = MaterialTheme.typography.titleLarge,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      if (!state) {
        Spacer(modifier = Modifier.height(4.dp))
        // 显示部分正文
        Text(
          text = article.content,
          style = MaterialTheme.typography.bodySmall,
          maxLines = 3,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}


@Preview(showBackground = true)
@Composable
fun ArticlePreview() {
  val articles = listOf(
    Article(
      id = 0,
      title = "Article Title",
      content = "" +
              "如果應用程式需要處理大量結構化資料， 將資料保存在本機最常見的用途是快取相關的 以便在裝置無法存取網路時 仍可在離線狀態下瀏覽這些內容\n" +
              "\n" +
              "Room 持續性程式庫透過 SQLite 提供抽象層， 可以順暢存取資料庫，並充分發揮 SQLite 的效用我們要用 Room 具有以下優點：\n" +
              "\n" +
              "提供 SQL 查詢的編譯時間驗證。\n" +
              "盡可能減少重複且容易出錯的樣板的便利註解 再也不是件繁重乏味的工作\n" +
              "簡化資料庫遷移路徑。\n" +
              "基於這些考量，我們強烈建議改用 Room 直接使用 SQLite API。\n",
      imageUrl = "https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEgiEwahggVQFLRook98z_RvVeMSmRMiiXr8lgJba3SF28oZ4bBNWeAmUWm2QA5I_bKfXMT5xzuHzQfEsyYfREgMhMObvoRYEH7OpEocvOWQMvrxwXh1tFmZkcQjruxtvamnyQRHNHI1mXusS36uMdoVYPZbiWKFr7v_ZTiuj3-xWnr9GyihxoT07LOrcAU/s1600/image5.png"
    ),
    Article(
      id = 1,
      title = "another Article",
      content = """Room 有三個主要元件：

保留目標的資料庫類別 並做為 應用程式保留資料。
代表的資料實體 納入應用程式資料庫中的資料表。
資料存取物件 (DAO)， 為應用程式提供可用於查詢、更新、插入及刪除的方法 儲存資料庫資料
資料庫類別為應用程式提供與 建立資料庫反過來，應用程式可以使用 DAO 從 視為關聯資料實體物件的執行個體。這個應用程式也可以 使用定義的資料實體更新對應資料表中的資料列，或者 即可建立要插入的新資料列。圖 1 說明瞭 Room 的不同元件
      實作範例
本節說明採用單一標頭的 Room 資料庫實作範例 以及單一 DAO

資料實體
以下程式碼定義 User 資料實體。「User」的每個例項 代表應用程式資料庫中 user 資料表的某一列。
      """.trimMargin(),
      imageUrl = "https://developer.android.com/static/develop/ui/compose/images/m3-typography.png"
    ),
    Article(
      id = 2,
      title = "third Article",
      content = """
        如要進一步瞭解 DAO，請參閱使用 Room 存取資料 DAO。

資料庫
以下程式碼定義了用於保存資料庫的 AppDatabase 類別。 AppDatabase 定義了資料庫設定，並做為應用程式的主要設定 存取保留點。資料庫類別必須符合 下列情況：

類別必須使用 @Database 註解， 包含 entities 陣列，列出所有與資料庫相關聯的資料實體。
類別必須是可擴充的抽象類別 RoomDatabase。
針對與資料庫關聯的每個 DAO 類別，系統會提供資料庫類別 必須定義無引數並傳回例項的抽象方法 DAO 類別
      """.trimMargin(),
      imageUrl = "https://raw.github.com/android/architecture-samples//main//screenshots/screenshots.png"
    ),
    Article(
      id = 3,
      title = "fourth Article",
      content = """Article content preview...
        |1245646546464645
        |446611231564568
      """.trimMargin(),
      imageUrl = "https://raw.github.com/android/nowinandroid//main//docs/images/screenshots.png"
    )
  )
  val selectedArticle by remember { mutableStateOf(MutableList(articles.size) { false }) }
  val navController = rememberNavController()
  NavHost(navController = navController, startDestination = "home") {
    composable("home") {
      ArticleListScreen(articles = articles, articlesState = selectedArticle) { article ->
        navController.navigate(article.id.toString())
      }
    }
    for (article in articles.withIndex()) {
      composable(article.value.id.toString()) {
        ArticlePage(
          nav = navController,
          article = article.value,
          selectedListRef = selectedArticle,
          selectedListIndex = article.index,
          onClick = { },
        )
      }
    }
  }
}

@Composable
fun ArticlePage(
  nav: NavHostController,
  article: Article,
  selectedListRef: MutableList<Boolean>,
  selectedListIndex: Int,
  onClick: () -> Unit,
) {
  var selected by remember { mutableStateOf(selectedListRef.get(selectedListIndex)) }
  Column(modifier = Modifier.padding(16.dp)) {
    Text(text = article.title, style = MaterialTheme.typography.titleLarge)
    Text(text = article.content, style = MaterialTheme.typography.bodySmall)
    Button(
      modifier = Modifier,
      content = { Text(text = "返回") },
      onClick = { nav.navigate("home") }
    )
    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
      RadioButton(
        selected = selected,
        onClick = {
          selected = !selected
          selectedListRef.set(selectedListIndex, !selectedListRef.get(selectedListIndex))
          onClick()
        }
      )
      Text(text = "折叠文章", modifier = Modifier.fillMaxWidth())
    }
  }
}