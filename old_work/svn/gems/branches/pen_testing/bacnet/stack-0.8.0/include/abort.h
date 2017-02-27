/**************************************************************************
*
* Copyright (C) 2012 Steve Karg <skarg@users.sourceforge.net>
*
* Permission is hereby granted, free of charge, to any person obtaining
* a copy of this software and associated documentation files (the
* "Software"), to deal in the Software without restriction, including
* without limitation the rights to use, copy, modify, merge, publish,
* distribute, sublicense, and/or sell copies of the Software, and to
* permit persons to whom the Software is furnished to do so, subject to
* the following conditions:
*
* The above copyright notice and this permission notice shall be included
* in all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
* CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
* TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
* SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*********************************************************************/
#ifndef ABORT_H
#define ABORT_H

#include <stdint.h>
#include <stdbool.h>

#ifdef __cplusplus_not 
extern "C" {
#endif /* __cplusplus_not*/

    BACNET_ABORT_REASON abort_convert_error_code(
        BACNET_ERROR_CODE error_code);

    int abort_encode_apdu(
        uint8_t * apdu,
        uint8_t invoke_id,
        uint8_t abort_reason,
        bool server);

    int abort_decode_service_request(
        uint8_t * apdu,
        unsigned apdu_len,
        uint8_t * invoke_id,
        uint8_t * abort_reason);

#ifdef TEST
#include "ctest.h"
    int abort_decode_apdu(
        uint8_t * apdu,
        unsigned apdu_len,
        uint8_t * invoke_id,
        uint8_t * abort_reason,
        bool * server);

    void testAbort(
        Test * pTest);
#endif

#ifdef __cplusplus_not
}
#endif /* __cplusplus_not*/
#endif