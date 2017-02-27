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

#include <stdbool.h>
#include "genObj.h"
#include "elf_objects.h"

typedef struct
{
    ObjectTypeDescriptor            objectTypeDescriptor;           // this must be first
    float                           OOSshadowPV;                    // todonext4 - use 'packed' emm structures for this (shadowPV, Priority array, relinquish default, etc.)
    float                           Prior_Value;                    // Cannot reuse shadowPV because it is possible to make multiple small changes to shadowPV before triggering analog COV (unlike binary case)
    float                           COV_Increment;                  // todonext3 - allow users to set default COV_Increments.
    float                           presentValue ;
} AnalogObjectTypeDescriptor;

typedef struct
{
    AnalogObjectTypeDescriptor      analogTypeDescriptor;           // this must be first
    float                           priorityArray[16] ;
    bool                            priorityAsserted[16] ;
    float                           relinquishDefault ;
} AnalogCommandableTypeDescriptor;           // todo 5 - we can probably eliminate this. see 31e6cbdb738bf3b247e6f111eda4634e42bfa41d


AnalogObjectTypeDescriptor *elf_get_analog_object_live_data_old(const ts_elf_template_object_t *currentObject, const uint32_t objectInstance );
AnalogObjectTypeDescriptor* elf_get_analog_object_live_data(const uint32_t deviceInstance, const ts_elf_template_object_t *ptr, const uint32_t objectInstance);
