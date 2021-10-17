package main

import (
	"fmt"
	"net/http"
	"net/http/httputil"
)

func main() {
	resp, err := http.Get("https://registry-1.docker.io/v2/")
	fmt.Printf("Error: %v\n", err)
	if err == nil {
		dr, _ := httputil.DumpResponse(resp, true)
		fmt.Printf("Body:\n===\n%s\n===\n", string(dr))
	}
}
