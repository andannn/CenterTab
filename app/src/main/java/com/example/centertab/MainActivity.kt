package com.example.centertab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.centertab.ui.theme.CenterTabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CenterTabTheme {
                var selectedIndex by remember {
                    mutableStateOf(2)
                }
                Surface(modifier = Modifier.fillMaxSize()) {
                    CenterTabLayout(
                        modifier = Modifier.fillMaxSize(),
                        selectedIndex = selectedIndex,
                        onScrollFinishToSelectIndex = {
                            selectedIndex = it
                        }
                    ) {
                        repeat(6) { index ->
                            Tab(
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                    .clip(CircleShape),
                                selected = index == selectedIndex,
                                selectedContentColor = MaterialTheme.colorScheme.primary,
                                unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                                onClick = {
                                    selectedIndex = index
                                },
                                text = @Composable {
                                    Text(text = "item $index")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
