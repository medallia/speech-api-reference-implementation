# Medallia Speech PoC Reference Implementation

## Usage

First, modify the `upload.sh` file to include the `API_TOKEN` and
`CALLBACK` values provided by your Medallia contact.

Second, modify the `upload-all.sh` file based on your system's pull
mechanism (local file, AWS, etc.)  The default implementation assumes
`.wav` files are located in the current directory.

Third, upload a single test file:

```
./upload.sh my-test-file.wav
```

Have your Medallia contact verify that the test file was received and
looks good.

Once verified, upload the remaining files:

```
./upload-all.sh
```

Again, coordinate with your Medallia contact to verify that the data
stream looks good.

## Dependencies

This script assumes execution on a standard Linux-based system.
Utilities such as `curl`, `file`, and `aws` are assumed to exist and be
in the current PATH.  If you run a different system or are missing
dependencies, please adjust the script or your system as needed.

## License

Copyright 2020.  Medallia, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may
not use this file except in compliance with the License.  You may obtain
a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
