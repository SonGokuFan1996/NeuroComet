$resDir = "C:\Users\bkyil\AndroidStudioProjects\NeuroComet\app\src\main\res"
$outFile = "C:\Users\bkyil\AndroidStudioProjects\NeuroComet\tools\missing_keys.txt"

# Parse base English strings
[xml]$base = Get-Content "$resDir\values\strings.xml" -Raw
$baseKeys = [ordered]@{}
foreach ($node in $base.resources.ChildNodes) {
    if ($node.LocalName -eq 'string' -and $node.GetAttribute('name')) {
        $key = $node.GetAttribute('name')
        $val = $node.InnerXml
        $baseKeys[$key] = $val
    }
    elseif ($node.LocalName -eq 'plurals' -and $node.GetAttribute('name')) {
        $key = $node.GetAttribute('name')
        $baseKeys[$key] = "PLURALS"
    }
    elseif ($node.LocalName -eq 'string-array' -and $node.GetAttribute('name')) {
        $key = $node.GetAttribute('name')
        $baseKeys[$key] = "STRING_ARRAY"
    }
}

$output = "Base English has $($baseKeys.Count) entries`r`n`r`n"

foreach ($lang in @('tr','es','de','fr','pt')) {
    $langFile = "$resDir\values-$lang\strings.xml"
    [xml]$langXml = Get-Content $langFile -Raw
    $langKeys = @{}
    foreach ($node in $langXml.resources.ChildNodes) {
        if ($node.GetAttribute -and $node.GetAttribute('name')) {
            $langKeys[$node.GetAttribute('name')] = $true
        }
    }

    $missing = [System.Collections.ArrayList]@()
    foreach ($k in $baseKeys.Keys) {
        if (-not $langKeys.ContainsKey($k)) {
            [void]$missing.Add($k)
        }
    }

    $output += "=== $lang === $($langKeys.Count)/$($baseKeys.Count) (missing: $($missing.Count))`r`n"
    foreach ($k in $missing) {
        $output += "  $k`r`n"
    }
    $output += "`r`n"
}

[System.IO.File]::WriteAllText($outFile, $output, [System.Text.UTF8Encoding]::new($false))

