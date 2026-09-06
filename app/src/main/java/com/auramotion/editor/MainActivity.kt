package com.auramotion.editor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.auramotion.editor.ui.screens.EditorScreen
import com.auramotion.editor.ui.screens.ProjectBrowserScreen
import com.auramotion.editor.ui.theme.AuraMotionTheme
import com.auramotion.editor.ui.theme.DarkBg
import com.auramotion.editor.viewmodel.EditorViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: EditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AuraMotionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBg
                ) {
                    var currentScreen by remember { mutableStateOf("editor") } // "browser" or "editor"

                    if (currentScreen == "browser") {
                        ProjectBrowserScreen(
                            onOpenProject = { currentScreen = "editor" }
                        )
                    } else {
                        EditorScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "browser" }
                        )
                    }
                }
            }
        }
    }
}
