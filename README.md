# Lazy Multi-GPU Tiled Deconvolution for ImageJ/Fiji

[![Build Status](https://github.com/BIOP/ijp-bdv-deconvolution/actions/workflows/build.yml/badge.svg)](https://github.com/BIOP/ijp-bdv-deconvolution/actions/workflows/build.yml)

A high-performance ImageJ plugin for GPU-accelerated Richardson-Lucy deconvolution of large microscopy images using [BigDataViewer-Playground](https://github.com/bigdataviewer/bigdataviewer-playground) and [CLIJ2](https://github.com/clij/clij2).

It is built on top of [CLIJ-FFT](https://github.com/clij/clij2-fft/).

## Overview

This plugin enables **lazy, tiled Richardson-Lucy deconvolution** of BigDataViewer sources directly on GPUs. It's designed to handle large datasets that don't fit in memory by processing them in configurable tiles with overlap.

### Key Features

- **GPU Acceleration**: Leverages CLIJ2 for fast deconvolution on the GPU
- **Tiled Processing**: Handles arbitrarily large images through lazy, tiled computation
- **BigDataViewer Integration**: Works with BDV sources
- **Flexible Output**: Keep original pixel type or output as float
- **Multi-threaded**: Parallel processing of tiles for maximum performance
- **Non-Circulant Option**: Support for non-circulant boundary conditions

## Installation

### Prerequisites

- [Fiji](https://fiji.sc/)
- One or several OpenCL capable devices (GPU, but not only)

### Using the Update Site

1. Open Fiji
2. Go to `Help > Update...`
3. Click `Manage Update Sites`
4. Add the following update sites: 
   - CLIJ & CLIJ2
   - CLIJ-Deconvolution
   - PTBIOP
   - (Optional: Quick Start CZI Reader for fast CZI reading)
5. Close and restart Fiji

## Usage

### Basic Workflow

1. **Load your data** as a BDV source (through BigDataViewer-Playground)
2. **Load your PSF** (Point Spread Function) as a BDV source
3. Run the deconvolution command:
    - Navigate to `Plugins > BigDataViewer-Playground > Sources > Deconvolve sources (Richardson Lucy GPU - Tiled)`
4. Configure the parameters (see below)
5. Click OK

The sources are lazily computed. Which means than any visualization or export will trigger the actual deconvolution computation.
### Parameters

| Parameter | Description | Recommended Values                              |
|-----------|-------------|-------------------------------------------------|
| **Select Source(s)** | Choose one or more BDV sources to deconvolve | -                                               |
| **Select PSF** | Choose the Point Spread Function | Single timepoint, highest resolution            |
| **Output Pixel Type** | Keep original type or convert to Float | Float for best quality, original to save memory |
| **Source Name Suffix** | Suffix added to deconvolved sources | `_deconvolved`                                  |
| **Block Size X/Y/Z** | Tile dimensions in pixels | 128-512 depending on GPU memory                 |
| **Overlap Size** | Overlap between tiles (pixels) | 16-64 (reduce edge artifacts)                   |
| **Number of Iterations** | Richardson-Lucy iterations | 20-100 (more = sharper but slower)              |
| **Non Circulant** | Use non-circulant boundary conditions | false for most cases                            |
| **Regularization Factor** | Prevent over-amplification of noise | 0.001-0.01                                      |
| **Number of Threads** | Parallel tile processing threads | 8-16                                            |

### Example: Deconvolving Lattice Light Sheet Data

```java
// Load your LLS7 data
File imageFile = new File("path/to/image.czi");
File psfFile = new File("path/to/psf.tif");

// Run deconvolution with recommended parameters
ij.command().run(SourcesDeconvolverCommand.class, true,
    "sacs", sources,
    "psf", psfSource,
    "output_pixel_type", "Float",
    "block_size_x", 256,
    "block_size_y", 256,
    "block_size_z", 256,
    "overlap_size", 32,
    "num_iterations", 40,
    "regularization_factor", 0.005f,
    "n_threads", 10
).get();
```

## How It Works

The plugin implements lazy, cached deconvolution that processes your image in tiles:

1. **Tile Division**: Image divided into overlapping blocks
2. **GPU Processing**: Each tile deconvolved on GPU using Richardson-Lucy algorithm
3. **Caching**: Results cached to avoid recomputation
4. **On-Demand**: Tiles computed only when needed for visualization
5. **Seamless Stitching**: Overlap regions ensure smooth transitions

This approach allows processing of images much larger than available GPU or RAM.

## Performance Tips

### Optimizing Block Size
- **Larger blocks**: Faster overall, but require more GPU memory
- **Smaller blocks**: More overhead, but work with limited GPU memory
- Start with 256×256×Z and adjust based on your GPU

### Optimizing Overlap
- **More overlap**: Better edge quality, slower processing
- **Less overlap**: Faster, but may show tile boundaries
- Recommended: 10-20% of block size

### GPU Memory Issues
If you encounter out-of-memory errors:
- Reduce block size
- Reduce number of iterations
- Close other GPU applications

## Technical Details

### Algorithm
Implements the **Richardson-Lucy** iterative deconvolution algorithm:
- Optimal for Poisson noise (typical in microscopy)
- Preserves positivity
- Can handle regularization

### Implementation
- Built on **CLIJ2** for GPU acceleration
- Uses **ImgLib2** cached cells for lazy processing
- Integrates with **BigDataViewer** for visualization
- Thread-safe multi-resolution support

## Requirements

- Java 8 or higher
- CUDA-capable GPU (NVIDIA)
- Sufficient GPU memory for block size + PSF

## Citation

If you use this plugin in your research, please cite:

```
Chiaruttini, N. (2024). GPU Tiled Deconvolution with BigDataViewer-Playground.
BioImaging and Optics Platform (BIOP), EPFL.
https://github.com/BIOP/ijp-bdv-deconvolution
```

## Support

- **Issues**: [GitHub Issues](https://github.com/BIOP/ijp-bdv-deconvolution/issues)
- **Forum**: [Image.sc Forum](https://forum.image.sc/)
- **Contact**: BIOP at EPFL

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the license headers in source files for details.

## Acknowledgments

- Developed at the [BioImaging and Optics Platform (BIOP)](https://biop.epfl.ch), EPFL
- Built on [CLIJ2](https://clij.github.io/) by Robert Haase
- Uses [BigDataViewer](https://imagej.net/plugins/bdv/) ecosystem
- Part of the [BigDataViewer-Playground](https://github.com/bigdataviewer/bigdataviewer-playground) suite

## Related Projects

- [BigDataViewer-Playground](https://github.com/bigdataviewer/bigdataviewer-playground)
- [CLIJ2](https://github.com/clij/clij2)
- [BigDataViewer-BIOP-Tools](https://github.com/BIOP/bigdataviewer-biop-tools)

---

**Maintained by**: Nicolas Chiaruttini ([@NicoKiaru](https://github.com/NicoKiaru))  
**Organization**: BIOP, EPFL  
**Year**: 2024-2025
