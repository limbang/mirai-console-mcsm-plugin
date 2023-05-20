package utils

import org.junit.Test
import top.limbang.mcsm.utils.toImage
import java.io.File

internal class ImageUtilsKtTest {

    @Test
    fun textToImage(){
        val image = "test test test test\ntest test test\ntest test\ntest test test\ntest".toImage()
        File("123.png").writeBytes(image.toByteArray())
    }
}