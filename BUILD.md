## Build (internal)

### Version

The build version is automatically incremented.  To determine the
current build version:

    build/version

### Package

Packaging builds the maven artifacts in `target/package` and installs
it into the local maven repository.  To package:

    build/package

### Deploy

Deployment requires that the AWS CLI tools be installed (see
https://aws.amazon.com/cli/).

The deploy script runs the package script, and then deploys the
artifacts to the S3 bucket "mrel".  To deploy:

    build/deploy

### Docs

To build and deploy api documentation:

    build/doc