package ch.epfl.biop.sourceandconverter;

import bdv.cache.SharedQueue;
import bdv.util.source.process.VoxelProcessedSource;
import bdv.viewer.SourceAndConverter;
import net.haesleinhuepf.clijx.imglib2cache.Clij2RichardsonLucyImglib2Cache;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.Cache;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.LoadedCellCacheLoader;
import net.imglib2.img.basictypeaccess.AccessFlags;
import net.imglib2.img.basictypeaccess.ArrayDataAccessFactory;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.PrimitiveType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import sc.fiji.bdvpg.cache.GlobalLoaderCache;
import sc.fiji.bdvpg.sourceandconverter.SourceAndConverterHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Deconvolver {

    public static <T extends RealType<T>> SourceAndConverter<FloatType> getDeconvolved(final SourceAndConverter<T> source,
                                                                                       //RandomAccessibleInterval<T> psf,
                                                                                       String name,
                                                                                       int[] cellDimensions,
                                                                                       Clij2RichardsonLucyImglib2Cache.Builder deconvolveBuilder,
                                                                                       SharedQueue queue) {

        List<Clij2RichardsonLucyImglib2Cache<FloatType, T, T>> ops = new ArrayList<>();

        int numTimepoints = SourceAndConverterHelper.getMaxTimepoint(source);

        // create the version of clij2 RL that works on cells
        for (int t = 0; t<numTimepoints; t++) {
            // One op per timepoint, but because the same builder is reused, the same gpu pool will be shared
            ops.add((Clij2RichardsonLucyImglib2Cache<FloatType, T, T>) deconvolveBuilder/*.psf(psf)*/.rai(source.getSpimSource().getSource(t,0)).build());
        }

        VoxelProcessedSource.Processor<T, FloatType> deconvolver =
                new VoxelProcessedSource.Processor<T, FloatType>() {

                    ConcurrentHashMap<Integer, ConcurrentHashMap<Integer,RandomAccessibleInterval<FloatType>>> cachedRAIs
                            = new ConcurrentHashMap<>();

                    RandomAccessibleInterval<FloatType> buildSource(RandomAccessibleInterval<T> rai, int t, int level) {

                        CellGrid grid = new CellGrid(rai.dimensionsAsLongArray(), cellDimensions);
                        FloatType type = new FloatType();
                        Cache<Long, Cell<?>> cache = (new GlobalLoaderCache(new Object(), t, level))
                                .withLoader(LoadedCellCacheLoader.get(grid, cell -> {
                                        ops.get(t).accept(cell);
                                }, type, AccessFlags.setOf(AccessFlags.VOLATILE)));
                        CachedCellImg img = new CachedCellImg(grid, type, cache, ArrayDataAccessFactory.get(PrimitiveType.BYTE, AccessFlags.setOf(AccessFlags.VOLATILE)));
                        return img;
                    }

                    @Override
                    public synchronized RandomAccessibleInterval<FloatType> process(RandomAccessibleInterval<T> rai, int t, int level) {
                        if (!cachedRAIs.containsKey(t)) {
                            cachedRAIs.put(t, new ConcurrentHashMap<>());
                        }
                        if (!cachedRAIs.get(t).containsKey(level)) {
                            cachedRAIs.get(t).put(level, buildSource(rai, t, level));
                        }
                        return cachedRAIs.get(t).get(level);
                    }
                };

        return new SourceVoxelProcessor<>(name, source, deconvolver, new FloatType(), queue).get();
    }
}
