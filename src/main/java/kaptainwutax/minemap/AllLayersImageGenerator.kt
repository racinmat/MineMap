package kaptainwutax.minemap

import kaptainwutax.featureutils.misc.SlimeChunk
import kaptainwutax.featureutils.structure.Mineshaft
import kaptainwutax.featureutils.structure.NetherFossil
import kaptainwutax.featureutils.structure.OceanRuin
import kaptainwutax.featureutils.structure.Shipwreck
import kaptainwutax.minemap.feature.OWBastionRemnant
import kaptainwutax.minemap.feature.OWFortress
import kaptainwutax.minemap.init.Configs
import kaptainwutax.minemap.init.Features
import kaptainwutax.minemap.init.Icons
import kaptainwutax.minemap.ui.DrawInfo
import kaptainwutax.minemap.ui.map.MapContext
import kaptainwutax.minemap.ui.map.MapSettings
import kaptainwutax.minemap.ui.map.fragment.Fragment
import kaptainwutax.seedutils.mc.Dimension
import kaptainwutax.seedutils.mc.MCVersion
import krangl.DataFrame
import krangl.asLongs
import krangl.readCSV
import java.awt.Color
import java.awt.font.TextAttribute
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.text.AttributedString
import javax.imageio.ImageIO

object AllLayersImageGenerator {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        //Initializes the icons, features and biome colors.
        Features.registerFeatures()
        Configs.registerConfigs()
        Icons.registerIcons()

        val version = MCVersion.v1_16_1
        val settings = MapSettings(version, Dimension.OVERWORLD).refresh()
        val settingsNether = MapSettings(version, Dimension.NETHER).refresh()
        Features.getForVersion(version).filter { !it.value.isValidDimension(Dimension.OVERWORLD) }.keys.forEach { settings.hide(it) }
        settings.hide(Shipwreck::class.java, OceanRuin::class.java, SlimeChunk::class.java, Mineshaft::class.java,
                OWBastionRemnant::class.java, OWFortress::class.java)
        Features.getForVersion(version).filter { !it.value.isValidDimension(Dimension.NETHER) }.keys.forEach { settingsNether.hide(it) }
        settingsNether.hide(NetherFossil::class.java)
        val seed = 386978361L
        val context = MapContext(seed, settings)
        (0 until context.biomeSource.layerCount).forEach f@{
            val outFile = File("E:\\Projects\\MinecraftSeedFinder\\layers_showing\\${it}.png")
            if (outFile.exists()) return@f
            context.layerId = it
            val fragment = Fragment(-2048, -2048, 4096, context)
            val screenshot = getScreenShot(fragment, 1024, 1024, seed)
            ImageIO.write(screenshot, "png", outFile)
        }
    }

    fun getScreenShot(fragment: Fragment, width: Int, height: Int, seed: Long): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val info = DrawInfo(0, 0, width, height)
        fragment.drawBiomes(image.graphics, info)
        fragment.drawFeatures(image.graphics, info)

        val as1 = AttributedString("seed: $seed")
        as1.addAttribute(TextAttribute.BACKGROUND, Color.BLACK)
        as1.addAttribute(TextAttribute.FONT, "arian_black")
        as1.addAttribute(TextAttribute.SIZE, 30)
        image.graphics.drawString(as1.iterator, 10, 30)


        return image
    }

}
