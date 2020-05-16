## How to config
Please edit the list of input papers in `paper_sources.xml`.

## Examples


Get all papers:

> http://localhost:8080/confdata/academicdata/papers

Get all the authors of the papers:
>  http://localhost:8080/confdata/academicdata/papers

"Expand" the list of papers. Store all referenced papers in the server for future computational demanding tasks, such as the network generation. Please run before you generate a network:
> http://localhost:8080/confdata/academicdata/papers/expand
> http://localhost:8080/confdata/academicdata/papers/expand/status
> http://localhost:8080/confdata/academicdata/papers/expand/cancel

Network:
> http://localhost:8080/confdata/academicdata/abc/network
> http://localhost:8080/confdata/academicdata/abc/
