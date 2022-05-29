package dev.arpan.shadows

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import dev.arpan.shadows.databinding.ActivityMaterialShapeBinding

fun Int.dp() = this * Resources.getSystem().displayMetrics.density

class MaterialShapeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMaterialShapeBinding

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaterialShapeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            sbTl.doOnProgressChanged { value ->
                tvTlValue.text = value.toString()
                progressChange()
            }
            sbTr.doOnProgressChanged { value ->
                tvTrValue.text = value.toString()
                progressChange()
            }
            sbBl.doOnProgressChanged { value ->
                tvBlValue.text = value.toString()
                progressChange()
            }
            sbBr.doOnProgressChanged { value ->
                tvBrValue.text = value.toString()
                progressChange()
            }

            sbElevation.doOnProgressChanged { value ->
                tvElevationValue.text = value.toString()
                view.setCardElevation(cardElevation = value.dp())
            }

            sbStrokeWidth.doOnProgressChanged { value ->
                tvStrokeWidthValue.text = value.toString()
                view.setStrokeWidth(strokeWidth = value.dp())
            }
        }
        progressChange()
    }

    private fun progressChange() {
        binding.apply {
            view.setCornerSizes(
                cornerSizeTopLeft = sbTl.progress.dp(),
                cornerSizeTopRight = sbTr.progress.dp(),
                cornerSizeBottomLeft = sbBl.progress.dp(),
                cornerSizeBottomRight = sbBr.progress.dp()
            )
        }
    }
}

private fun SeekBar.doOnProgressChanged(block: (progress: Int) -> Unit) {
    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            block(progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

        override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

    })
}