# CSV to XML

A generic script for use in Boomi to take a CSV file and to rip it into multiple XML Documents.

The XML documents are returned as individual documents back to Boomi.

Each CSV row is one XML Document.


There are two formatting functions: -

- returnDocumentNamedTags

Each CSV column is returned as a tag in the document using the column's name as the tag name.

The output looks like: -

<Record>
  <CsvColumnName1>value1</CsvColumnName1>
  <CsvColumnName2>value2</CsvColumnName2>
</Record>

Column names are camel cased to remove spaces.

- returnDocumentGenericTags

This returns each field in a more generic form of: -

<Record>
  <Field>
    <Name>value1</Name>
    <Value>value2</Value>
  </Field>
</Record>


It's your job then to map it to whatever you want.

