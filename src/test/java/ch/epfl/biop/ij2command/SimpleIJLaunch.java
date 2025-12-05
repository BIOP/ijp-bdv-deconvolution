/*-
 * #%L
 * Tiled GPU Deconvolution for BigDataViewer-Playground - BIOP - EPFL
 * %%
 * Copyright (C) 2024 - 2025 EPFL
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package ch.epfl.biop.ij2command;

import bdv.viewer.SourceAndConverter;
import ch.epfl.biop.DatasetHelper;
import ch.epfl.biop.bdv.img.bioformats.command.CreateBdvDatasetBioFormatsCommand;
import ch.epfl.biop.scijava.command.spimdata.LLS7OpenDatasetCommand;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clijx.plugins.clij2fftWrapper;
import net.imagej.ImageJ;
import org.apache.commons.io.FilenameUtils;
import sc.fiji.bdvpg.scijava.services.SourceAndConverterService;

import java.io.File;

public class SimpleIJLaunch {

    /**
     * This main function serves for development purposes.
     * It allows you to run the plugin immediately out of
     * your integrated development environment (IDE).
     *
     * @param args whatever, it's ignored
     * @throws Exception ExecutionException – if the computation threw an exception, InterruptedException – if the current thread was interrupted while waiting
     */
    public static void main(final String... args) throws Exception {


        //System.out.println(CLIJ2.clinfo());

        //CLIJ2.getInstance()

        System.out.println(System.getProperty("java.library.path"));

        clij2fftWrapper.diagnostic();

        //return;

        // create the ImageJ application context with all available services
        final ImageJ ij = new ImageJ();
        ij.ui().showUI();
        CLIJ.getAvailableDeviceNames().forEach(d -> System.out.println(d));


        demoDeconvolution(ij);
    }

    public static void demoDeconvolution(ImageJ ij) throws Exception {

        // launch IJ so we can interact with the inputs and outputs
        // ij.launch();


        //DatasetHelper.getDataset()
        File helaKyotoLLS7 = DatasetHelper.getDataset("https://zenodo.org/records/14505724/files/Hela-Kyoto-1-Timepoint-LLS7.czi");
        ij.command().run(LLS7OpenDatasetCommand.class, true,
                "czi_file", helaKyotoLLS7,
                "legacy_xy_mode", false).get();

        File psfLLS7 = DatasetHelper.getDataset("https://zenodo.org/records/14505724/files/psf-200nm.tif");
        ij.command().run(CreateBdvDatasetBioFormatsCommand.class, true,
                "files", new File[]{psfLLS7},
                "datasetname", "psf_lls7_200nm",
                "unit", "MICROMETER",
                "split_rgb_channels", false,
                "auto_pyramidize", false,
                "plane_origin_convention", "CENTER",
                "disable_memo", false
                ).get();


        /*Dataset psfD = (Dataset) ij.io()
                .open(psfLLS7.getAbsolutePath());*/

        //RandomAccessibleInterval<FloatType> psf = (RandomAccessibleInterval<FloatType>) psfD.getImgPlus();
        String datasetName = FilenameUtils.removeExtension(helaKyotoLLS7.getName());

        /*helaKyotoLLS7 = DatasetHelper.getDataset("https://zenodo.org/records/5101351/files/Raw_large.tif");
        psfLLS7 = DatasetHelper.getDataset("https://zenodo.org/records/5101351/files/PSFHuygens_confocal_Theopsf.tif");


        Dataset psfD = (Dataset) ij.io()
                .open(psfLLS7.getAbsolutePath());

        RandomAccessibleInterval<FloatType> psf = (RandomAccessibleInterval<FloatType>) psfD.getImgPlus();

        int radius = 32;

        psf = Views.interval(psf, new long[]{512-radius, 512-radius, 0}, new long[]{512+radius, 512+radius, 40});


        */

        /*ij.command().run(CreateBdvDatasetBioFormatsCommand.class, true,
                "files", new File[] {helaKyotoLLS7},
                "unit", "MICROMETER",
                "split_rgb_channels", false,
                "auto_pyramidize", false,
                "disable_memo", false,
                "datasetname", datasetName,
                "plane_origin_convention", "CENTER"
                ).get();*/




        SourceAndConverter source = ij.context().getService(SourceAndConverterService.class).getUI().getSourceAndConvertersFromPath(datasetName)
                .toArray(new SourceAndConverter[0])[0];

        /*SourceAndConverter deconvolved = Deconvolver.getDeconvolved(
                source,
                psf,
                "Deconv",
                new int[]{256,256,44},
                //new long[]{10,10,10},60,
                Runtime.getRuntime().availableProcessors()-1);

        BdvFunctions.show(deconvolved);

        ij.get(SourceAndConverterService.class).register(deconvolved);*/


    }


}
