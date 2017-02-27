#include "advdebug.h"
#include "elf_xml_parse.h"
#include "elf_std.h"

ELF_RETURN parse_xml(xml_parse_ctx_t *x)
{
    node_t **ans;
    int max;
    int j;

    x->num_results = 0 ;
    x->root = roxml_load_buf(x->xml_buf);
	if (x->root == NULL)
	{
		panic("Buffer does not contain valid xml document");
		return ELF_RETURN_FAIL ;
	}

    ans = roxml_xpath(x->root, (char *) x->xml_xpath, &max);
    if ( ans == NULL )
    {
        // failures are expected, so we can't panic here
        // panic ( "Roxml_parse failed for %s", x->xml_xpath );
	    roxml_close(x->root);
	    return ELF_RETURN_FAIL ;
    }

    x->num_results = max;

    x->results = (char **) malloc(x->num_results * sizeof(char *));
	if (x->results == NULL)
	{
		// fatal
		panic("Out of memory");
		exit(EXIT_FAILURE);
	}

    for (j = 0; j < max; j++)
    {
        x->results[j] = NULL;
        node_t *child;
        x->results[j] = roxml_get_content(ans[j], NULL, 0, NULL);
        if (x->results[j])
        {
            if (*(x->results[j]) == 0)
            {
                int i = 0;
                int nb_chld = roxml_get_chld_nb(ans[j]);
                for (i = 0; i < nb_chld; i++)
                {
                    child = roxml_get_chld(ans[j], NULL, i);
                    x->results[j] = roxml_get_name(child, NULL, 0);
                }
            }
        }
    }
	return ELF_RETURN_OK ;
}

