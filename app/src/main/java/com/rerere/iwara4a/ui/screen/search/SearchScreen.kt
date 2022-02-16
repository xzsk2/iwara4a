package com.rerere.iwara4a.ui.screen.search

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.rerere.iwara4a.R
import com.rerere.iwara4a.model.index.MediaPreview
import com.rerere.iwara4a.ui.local.LocalNavController
import com.rerere.iwara4a.ui.public.MediaPreviewCard
import com.rerere.iwara4a.ui.public.QueryParamSelector
import com.rerere.iwara4a.ui.public.SimpleIwaraTopBar
import com.rerere.iwara4a.ui.public.items
import com.rerere.iwara4a.util.noRippleClickable
import com.rerere.iwara4a.util.stringResource

@ExperimentalFoundationApi
@Composable
fun SearchScreen(searchViewModel: SearchViewModel = hiltViewModel()) {
    val navController = LocalNavController.current
    Scaffold(
        topBar = {
            SimpleIwaraTopBar(stringResource(R.string.search))
        }
    ) {
        val result = searchViewModel.pager.collectAsLazyPagingItems()

        Column(
            Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            SearchBar(searchViewModel, result)
            Result(navController, searchViewModel, result)
        }
    }
}

@ExperimentalFoundationApi
@Composable
private fun Result(
    navController: NavController,
    searchViewModel: SearchViewModel,
    list: LazyPagingItems<MediaPreview>
) {
    if (list.loadState.refresh !is LoadState.Error) {
        Crossfade(searchViewModel.query) {
            if (it.isNotBlank()) {
                SwipeRefresh(
                    state = rememberSwipeRefreshState(list.loadState.refresh == LoadState.Loading),
                    onRefresh = { list.refresh() },
                    indicator = { s, trigger ->
                        SwipeRefreshIndicator(
                            s,
                            trigger,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Column {
                        Box {
                            LazyVerticalGrid(
                                modifier = Modifier.fillMaxSize(),
                                cells = GridCells.Fixed(2)
                            ) {
                                items(list) {
                                    MediaPreviewCard(navController, it!!)
                                }

                                when (list.loadState.append) {
                                    LoadState.Loading -> {
                                        item {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                CircularProgressIndicator()
                                                Text(text = stringResource(id = R.string.loading))
                                            }
                                        }
                                    }
                                    is LoadState.Error -> {
                                        item {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .noRippleClickable { list.retry() }
                                                    .padding(16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = stringResource(id = R.string.load_error),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    else -> {}
                                }
                            }
                            if (list.loadState.refresh == LoadState.Loading && list.itemCount == 0) {
                                val composition by rememberLottieComposition(
                                    LottieCompositionSpec.RawRes(
                                        R.raw.dolphin
                                    )
                                )
                                LottieAnimation(
                                    modifier = Modifier
                                        .size(200.dp)
                                        .align(Alignment.Center),
                                    composition = composition,
                                    iterations = LottieConstants.IterateForever
                                )
                            }
                        }
                    }
                }
            } else {
                // 也许可以加个搜索推荐？
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .noRippleClickable { list.refresh() }, contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.error_state_dog))
                LottieAnimation(
                    modifier = Modifier.size(150.dp),
                    composition = composition,
                    iterations = LottieConstants.IterateForever
                )
                Text(text = stringResource(id = R.string.load_error), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SearchBar(searchViewModel: SearchViewModel, list: LazyPagingItems<MediaPreview>) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    Card(modifier = Modifier.padding(8.dp)) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = searchViewModel.query,
                    onValueChange = { searchViewModel.query = it.replace("\n", "") },
                    maxLines = 1,
                    placeholder = {
                        Text(text = stringResource(id = R.string.screen_search_bar_placeholder))
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    trailingIcon = {
                        if (searchViewModel.query.isNotEmpty()) {
                            IconButton(onClick = { searchViewModel.query = "" }) {
                                Icon(Icons.Default.Close, null)
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (searchViewModel.query.isBlank()) {
                                Toast.makeText(context, context.stringResource(id = R.string.screen_search_bar_empty), Toast.LENGTH_SHORT).show()
                            } else {
                                focusManager.clearFocus()
                                list.refresh()
                            }
                        }
                    )
                )
            }
            IconButton(onClick = {
                if (searchViewModel.query.isBlank()) {
                    Toast.makeText(context, context.stringResource(id = R.string.screen_search_bar_empty), Toast.LENGTH_SHORT).show()
                } else {
                    focusManager.clearFocus()
                    list.refresh()
                }
            }) {
                Icon(Icons.Default.Search, null)
            }
        }
    }
}