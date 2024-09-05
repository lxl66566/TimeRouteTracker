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
      content = "Article content preview",
      imageUrl = "https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEgiEwahggVQFLRook98z_RvVeMSmRMiiXr8lgJba3SF28oZ4bBNWeAmUWm2QA5I_bKfXMT5xzuHzQfEsyYfREgMhMObvoRYEH7OpEocvOWQMvrxwXh1tFmZkcQjruxtvamnyQRHNHI1mXusS36uMdoVYPZbiWKFr7v_ZTiuj3-xWnr9GyihxoT07LOrcAU/s1600/image5.png"
    ),
    Article(
      id = 1,
      title = "another Article",
      content = """Article content preview...
        |1245646546464645
        |446611231564568
        |456467489789465413165
        |46547897946321894
      """.trimMargin(),
      imageUrl = "https://developer.android.com/static/develop/ui/compose/images/m3-typography.png"
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