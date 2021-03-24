FROM zenika/kotlin

WORKDIR /detektDx

COPY PartialConverterForDetektOuputFormatToDxPlatformInputFormat.kt ./DetektDx.kt
COPY /detekt-cli-1.16.0 ./detekt-cli-1.16.0

RUN kotlinc DetektDx.kt
