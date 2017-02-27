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

#include "bacenum.h"

typedef struct _ObjectTypeDescriptor
{
    bool                Out_Of_Service;             // todo 4 - figure out how to use bitfields here
    bool                Change_Of_Value ;           // todo 4 - we can pack these - but BE VERY aware of possible race conditions
    BACNET_RELIABILITY  reliability;				// use GetReliability() from now on to account for other factors
	time_t				lastUpdate;
} ObjectTypeDescriptor;

