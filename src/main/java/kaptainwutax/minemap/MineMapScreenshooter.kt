package kaptainwutax.minemap

import java.io.IOException
import kotlin.jvm.JvmStatic
import kaptainwutax.minemap.init.Features
import kaptainwutax.minemap.init.Configs
import kaptainwutax.minemap.init.Icons
import kaptainwutax.minemap.ui.map.MapSettings
import kaptainwutax.seedutils.mc.MCVersion
import kaptainwutax.featureutils.structure.Shipwreck
import kaptainwutax.featureutils.structure.OceanRuin
import kaptainwutax.featureutils.misc.SlimeChunk
import kaptainwutax.featureutils.structure.Mineshaft
import kaptainwutax.minemap.ui.map.MapContext
import java.awt.image.BufferedImage
import kaptainwutax.minemap.MineMapScreenshooter
import javax.imageio.ImageIO
import java.io.File
import kaptainwutax.minemap.ui.DrawInfo
import kaptainwutax.minemap.ui.map.fragment.Fragment
import kaptainwutax.seedutils.mc.Dimension
import krangl.DataFrame
import krangl.asLongs
import krangl.readCSV
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.FileReader
import java.io.Reader

object MineMapScreenshooter {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        //Initializes the icons, features and biome colors.
        Features.registerFeatures()
        Configs.registerConfigs()
        Icons.registerIcons()
        val settings = MapSettings(MCVersion.v1_16_1, Dimension.OVERWORLD).refresh()
        settings.hide(Shipwreck::class.java, OceanRuin::class.java, SlimeChunk::class.java, Mineshaft::class.java)

        val csvName = "distances_all_structs_close"
        val seedsDf = DataFrame.readCSV("E:\\Projects\\MinecraftSeedFinder\\${csvName}.csv")
        val seeds = seedsDf["seed"].asLongs()
        seeds.forEach { seed->
            val context = seed?.let { MapContext(it, settings) }
            val fragment = Fragment(-2048, -2048, 4096, context)
            val screenshot = getScreenShot(fragment, 1024, 1024)
            File("E:\\Projects\\MinecraftSeedFinder\\${csvName}").mkdir()
            ImageIO.write(screenshot, "png", File("E:\\Projects\\MinecraftSeedFinder\\${csvName}\\${context?.worldSeed}.png"))
        }

    }

    fun getScreenShot(fragment: Fragment, width: Int, height: Int): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val info = DrawInfo(0, 0, width, height)
        fragment.drawBiomes(image.graphics, info)
        fragment.drawFeatures(image.graphics, info)
        return image
    }

}