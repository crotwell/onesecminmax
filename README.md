# onesecminmax
Calculate min and max per second and output as a 2 sps channel

For example:

   bin/onesecminmax -o minmaxdir -f data.mseed

would create 2 sps miniseed records, for the recoreds in data.mseed. The output is put in a bud-like directory structure
of net/sta/year/jday/ with 1 hour mseed files.
