package com.rinkesh.masking

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.rinkesh.masking.ui.theme.AadhaarMaskingTheme
import com.rinkesh.maskinglib.MaskingManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AadhaarMaskingTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainComposable()
                }
            }
        }
    }

    // Registers a photo picker activity launcher in single-select mode.

    @Composable
    fun MainComposable(modifier: Modifier = Modifier) {
        var fileName by remember { mutableStateOf<File?>(null) }
        var isLoading by remember { mutableStateOf<Boolean>(false) }

        val pickMedia =
            rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    Log.d("PhotoPicker", "Selected URI: $uri")
                    isLoading = true
                    lifecycleScope.launch(Dispatchers.Default) {
                        fileName = MaskingManager.processImageForMasking(this@MainActivity, uri)
                        isLoading = false
                    }

                } else {
                    Log.d("PhotoPicker", "No media selected")
                }

            }

        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

            }) {
                Text("Select Aadhaar Card from gallery")
            }
            Spacer(modifier = Modifier.height(32.dp))
            if (isLoading) {
                CircularProgressIndicator()
            }
            if (fileName != null) {
                AsyncImage(
                    model = fileName,
                    contentDescription = "Image from photo picker",
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}
