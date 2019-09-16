
echo "Building mochi"
cd ..
./scripts/build-pre-submit.sh
cd scripts/

echo "Checking if azure-cli brew package is installed"
if brew ls --versions azure-cli > /dev/null; then
  echo "azure-cli package is installed"
else
  brew install azure-cli
fi

echo "Creating local maven repository..."
#replace release with the expected version
export release=3.1.0

rm -rf android/
mvn install:install-file -Dfile=../publisher-sdk/build/outputs/aar/publisher-sdk-release.aar -DgroupId=com.criteo.publisher -DartifactId=CriteoPublisherSdk_Android_v${release}_release -Dversion=$release -Dpackaging=aar -DgeneratePOM=true -DlocalRepositoryPath=android/

export AZURE_STORAGE_ACCOUNT=pubsdkuseprod
export AZURE_STORAGE_KEY=IBXkbamPEDzFFvLFgjL8bG5v7GOLy/2HY2xMVtgXICxSXG/AYYP57Xme9lxNgcoaznc2XGdye/zDT7fPUYrXbA==

export container_name=publishersdk
export aar_blob_name=android/com/criteo/publisher/CriteoPublisherSdk_Android_v${release}_release/$release/CriteoPublisherSdk_Android_v${release}_release-${release}.aar
export aar_file_to_upload=android/com/criteo/publisher/CriteoPublisherSdk_Android_v${release}_release/$release/CriteoPublisherSdk_Android_v${release}_release-${release}.aar

export pom_blob_name=android/com/criteo/publisher/CriteoPublisherSdk_Android_v${release}_release/$release/CriteoPublisherSdk_Android_v${release}_release-${release}.pom
export pom_file_to_upload=android/com/criteo/publisher/CriteoPublisherSdk_Android_v${release}_release/$release/CriteoPublisherSdk_Android_v${release}_release-${release}.pom

export mvn_blob_name=android/com/criteo/publisher/CriteoPublisherSdk_Android_v${release}_release/maven-metadata-local.xml
export mvn_file_to_upload=android/com/criteo/publisher/CriteoPublisherSdk_Android_v${release}_release/maven-metadata-local.xml

echo "Uploading the aar file: ${aar_blob_name}"
az storage blob upload --container-name $container_name --file $aar_file_to_upload --name $aar_blob_name

echo "Uploading the pom file: ${pom_blob_name}"
az storage blob upload --container-name $container_name --file $pom_file_to_upload --name $pom_blob_name

echo "Uploading the maven metadata local file: ${mvn_blob_name}"
az storage blob upload --container-name $container_name --file $mvn_file_to_upload --name $mvn_blob_name

echo "Listing the blobs..."
az storage blob list --container-name $container_name --output table

echo "Done"
