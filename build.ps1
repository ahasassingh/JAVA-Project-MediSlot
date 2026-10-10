# Helper script to compile and run MediSlot without system Maven

$JAR_MYSQL = "C:\Users\Admin\.m2\repository\com\mysql\mysql-connector-j\8.0.33\mysql-connector-j-8.0.33.jar"
$JAR_FLATLAF = "C:\Users\Admin\.m2\repository\com\formdev\flatlaf\3.4\flatlaf-3.4.jar"
$CP = "$JAR_MYSQL;$JAR_FLATLAF;target/classes"

if (!(Test-Path "target/classes")) {
    New-Item -ItemType Directory -Force -Path "target/classes" | Out-Null
}

Copy-Item -Path "src/main/resources/*" -Destination "target/classes" -Force -Recurse

Write-Host "Compiling Java 21+ source files..." -ForegroundColor Cyan
$sources = Get-ChildItem -Path "src/main/java" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
javac -d target/classes -cp "$JAR_MYSQL;$JAR_FLATLAF" $sources

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation SUCCESSFUL!" -ForegroundColor Green
    if ($args[0] -eq "run") {
        Write-Host "Launching MediSlot UI..." -ForegroundColor Yellow
        java -cp $CP com.medislot.Main
    }
} else {
    Write-Host "Compilation FAILED!" -ForegroundColor Red
}
