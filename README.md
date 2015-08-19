# KotlinFrontendPerforanceCounter
Build jar. Run it inside kotlin project directory. The program will execute `ant dist` 5 times, on each run extract PERF ANALYZE for targets and then calculate average value.

Output example:

`=> Tests completed`

`=> Tests number: 5`

`=> Analysis duration per target (all in ms):`

`> android-compiler-plugin:`

`  all values: [812.0, 729.0, 741.0, 860.0, 835.0]`

`  mean: 795.4`

`  variance: 2677.04`

`> pack-runtime:`

`  all values: [114.0, 109.0, 157.0, 160.0, 117.0]`

`  mean: 131.4`

`  variance: 497.03999999999996`

`...`

`> compiler:`

`  all values: [37512.0, 35690.0, 39785.0, 39904.0, 35669.0]`

`  mean: 37712.0`

`  variance: 3480905.2`
