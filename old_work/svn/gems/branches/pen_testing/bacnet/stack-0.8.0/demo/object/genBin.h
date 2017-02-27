/**************************************************************************
*
* Copyright (C) 2015 ConnectEx, Inc.
*
* Permission is hereby granted to the licensee for use
* of this software and associated documentation files (the
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
*
*********************************************************************/

#pragma once

#include "genObj.h"

typedef struct
{
    ObjectTypeDescriptor            objectTypeDescriptor;           // this must be first
    BACNET_BINARY_PV                OOSshadowPV;
    BACNET_BINARY_PV                Prior_Value;
    BACNET_POLARITY                 inputPolarity;
    BACNET_BINARY_PV                presentValue;
} BinaryObjectTypeDescriptor;

typedef struct
{
    BinaryObjectTypeDescriptor      binaryTypeDescriptor;           // this must be first
    bool                            priorityArray[16] ;
    bool                            priorityAsserted[16] ;
    bool                            relinquishDefault ;
} BinaryCommandableTypeDescriptor;


BinaryObjectTypeDescriptor *elf_get_binary_object_live_data_old(const ts_elf_template_object_t *currentObject, const uint32_t objectInstance);
BinaryObjectTypeDescriptor *elf_get_binary_object_live_data(const uint32_t deviceInstance, const ts_elf_template_object_t *currentObject, const uint32_t objectInstance);
