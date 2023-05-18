/*
 * Copyright (c) 2023 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcsm-plugin/blob/master/LICENSE
 */

package top.limbang.mcsm.utils

import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 * 文本转成白底黑字的图片
 *
 * @return
 */
fun String.toImage() = textToImage(this)

/**
 * 文本转成白底黑字的图片
 *
 * @param text
 * @return
 */
fun textToImage(text: String): ByteArrayOutputStream {
    //按照换行符分割文本，并设置字体、行距等参数
    val textList = text.split("\n")
    val g2d = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).createGraphics()
    g2d.font = Font("宋体", Font.PLAIN, 20)
    val fm = g2d.fontMetrics
    val lineHeight = fm.height

    //计算每行文本的宽度和高度，并找出最长一行的宽度
    var maxWidth = 0
    for (line in textList) {
        val lineWidth = fm.stringWidth(line)
        if (lineWidth > maxWidth) {
            maxWidth = lineWidth
        }
    }
    val imageWidth = maxWidth + 20 //左右各留 10 像素的边距
    val imageHeight = lineHeight * textList.size + 20 //上下各留 10 像素的边距

    //创建一个新的 BufferedImage 对象并获取 Graphics2D 对象
    val newImage = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB)
    val newG2d = newImage.createGraphics()

    //循环绘制每行文本到新的 BufferedImage 上，并释放资源
    newG2d.color = Color.WHITE
    newG2d.fillRect(0, 0, imageWidth, imageHeight) //先用白色填充整个区域
    newG2d.font = g2d.font //复制之前的字体
    newG2d.color = Color.BLACK //设置字体颜色为黑色
    for ((index, line) in textList.withIndex()) {
        newG2d.drawString(line, 10, lineHeight * (index + 1)) //在 (10, lineHeight * (index + 1)) 的位置开始绘制文本
    }
    newG2d.dispose()
    g2d.dispose()

    val byteArrayOutputStream = ByteArrayOutputStream()
    ImageIO.write(newImage, "png", byteArrayOutputStream)
    return byteArrayOutputStream
}

/**
 * ### ByteArrayOutputStream 转 ByteArrayInputStream
 *
 */
fun ByteArrayOutputStream.toInput(): ByteArrayInputStream {
    return ByteArrayInputStream(this.toByteArray())
}
