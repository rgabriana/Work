#ifndef _ELF_XML_PARSE_H_
#define _ELF_XML_PARSE_H_

#include "elf_std.h"

#ifdef __cplusplus
extern "C" {
#include <roxml.h>
}
#else
#include <roxml.h>
#endif

typedef struct xml_parse_ctx
{
	node_t *root;
	// inputs
	char *xml_buf;
	const char *xml_xpath;
	// outputs
	int num_results;
	char **results;
} xml_parse_ctx_t;

ELF_RETURN parse_xml(xml_parse_ctx_t *x);

#endif
