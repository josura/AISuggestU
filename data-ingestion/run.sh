#!/bin/bash
docker run -it -e GITHUB_TOKEN="a6f2e50dd2e36b24ad1ac7fb97332335b99d84ac" -v "$(pwd)":/shared parser /bin/bash