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
import kaptainwutax.minemap.feature.OWBastionRemnant
import kaptainwutax.minemap.feature.OWFortress
import kaptainwutax.minemap.ui.map.MapContext
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File
import kaptainwutax.minemap.ui.DrawInfo
import kaptainwutax.minemap.ui.map.fragment.Fragment
import kaptainwutax.seedutils.mc.Dimension
import krangl.DataFrame
import krangl.asLongs
import krangl.readCSV

object MineMapScreenshooter {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        //Initializes the icons, features and biome colors.
        Features.registerFeatures()
        Configs.registerConfigs()
        Icons.registerIcons()

        val version = MCVersion.v1_16_1
        val settings = MapSettings(version, Dimension.OVERWORLD).refresh()
        Features.getForVersion(version)
        val features2hide = Features.getForVersion(version).filter { !it.value.isValidDimension(Dimension.OVERWORLD) }.keys
        features2hide.forEach { settings.hide(it)}
        settings.hide(Shipwreck::class.java, OceanRuin::class.java, SlimeChunk::class.java, Mineshaft::class.java,
                OWBastionRemnant::class.java, OWFortress::class.java)


//        val csvName = "distances_all_structs_close"
//        val csvName = "distances_all_structs_biomes_close"
//        val csvName = "distances_village_mansion_igloo_desert_pyramid_swamp_hut_close"
        val csvName = "distances_village_mansion_desert_pyramid_close"
//        val csvName = "good_seeds_old_2\\distances_all_merged_top"
        val seedsDf = DataFrame.readCSV("E:\\Projects\\MinecraftSeedFinder\\${csvName}.csv")
        val seeds = seedsDf["seed"].asLongs()
        seeds.forEach { seed ->
            val outFile = File("E:\\Projects\\MinecraftSeedFinder\\${csvName}\\${seed}.png")
            if (outFile.exists()) return@forEach
            val context = seed?.let { MapContext(it, settings) }
            val fragment = Fragment(-2048, -2048, 4096, context)
            val screenshot = getScreenShot(fragment, 1024, 1024)
            File("E:\\Projects\\MinecraftSeedFinder\\${csvName}").mkdir()
            ImageIO.write(screenshot, "png", outFile)
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
