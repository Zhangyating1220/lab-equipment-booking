$sqlContent = Get-Content -Path "doc/sql/schema.sql" -Raw
$sqlContent = $sqlContent -replace "CREATE DATABASE IF NOT EXISTS lab_booking;`r?`nUSE lab_booking;`r?`n", ""
$sqlStatements = $sqlContent -split ";" | Where-Object { $_.Trim() -ne "" }

foreach ($sql in $sqlStatements) {
    $sql = $sql.Trim()
    if ($sql -ne "") {
        & "C:\Program Files\MySQL\MySQL Server 9.7\bin\mysql.exe" -u root -pZyt041109 lab_booking -e $sql
    }
}