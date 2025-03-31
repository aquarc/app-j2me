# app-j2me
application developed for j2me-compatible phones

## install j2me in 2025
install java8, you can use zulu if you are on mac
```
export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
```

## build
get the library files from the same place as discord j2me. You can also develop your own j2me apps using these libraries.
```sh
curl -O https://github.com/vipaoL/j2me-build-tools/raw/c1598b6916f2ba2ad5be1c0accd1ed2a54c156f3/WTK2.5.2/lib/midpapi20.jar
curl -O https://github.com/vipaoL/j2me-build-tools/raw/c1598b6916f2ba2ad5be1c0accd1ed2a54c156f3/WTK2.5.2/lib/cldcapi10.jar
curl -O https://github.com/vipaoL/j2me-build-tools/raw/c1598b6916f2ba2ad5be1c0accd1ed2a54c156f3/WTK2.5.2/lib/jsr75.jar
curl -O https://nnp.nnchan.ru/pna/lib/javapiglerapi.jar
curl -O https://github.com/vipaoL/j2me-build-tools/raw/refs/heads/master/lib/nokiaui.jar
```

Then git clone NNJSON inside the directory:

```sh 
git clone https://github.com/aquarc/app-j2me
cd app-j2me

git clone https://github.com/shinovon/NNJSON
```

## build

```sh
export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
./build.sh
```
You can use `MicroEmulator` to emulate it.
