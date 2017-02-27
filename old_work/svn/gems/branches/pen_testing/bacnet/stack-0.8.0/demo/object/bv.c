/**************************************************************************
*
* Copyright (C) 2006 Steve Karg <skarg@users.sourceforge.net>
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
*
*********************************************************************/

/* Binary Value Objects - customize for your use */

#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>

#include "bacdef.h"
#include "bacdcode.h"
#include "bacenum.h"
#include "bacapp.h"
#include "config.h"     /* the custom stuff */
#include "handlers.h"
#include "bv.h"
#include "advdebug.h"
#include "elf_gems_api.h"
#include "elf.h"
#include "elf_functions.h"

/* These three arrays are used by the ReadPropertyMultiple handler */
static const int Binary_Value_Properties_Required[] = {
	PROP_OBJECT_IDENTIFIER,
	PROP_OBJECT_NAME,
	PROP_OBJECT_TYPE,
	PROP_PRESENT_VALUE,
	PROP_STATUS_FLAGS,
	PROP_EVENT_STATE,
	PROP_OUT_OF_SERVICE,
	-1
};

static const int Binary_Value_Properties_Optional[] = {
	PROP_DESCRIPTION,
	PROP_PRIORITY_ARRAY,
	PROP_RELINQUISH_DEFAULT,
	-1
};

static const int Binary_Value_Properties_Proprietary[] = {
	-1
};

void Binary_Value_Property_Lists(
    const int **pRequired,
	const int **pOptional,
	const int **pProprietary)
{
	if (pRequired)
		*pRequired = Binary_Value_Properties_Required;
	if (pOptional)
		*pOptional = Binary_Value_Properties_Optional;
	if (pProprietary)
		*pProprietary = Binary_Value_Properties_Proprietary;

	return;
}


void Binary_Value_Init(
    void)
{
}

/* we simply have 0-n object instances.  Yours might be */
/* more complex, and then you need validate that the */
/* given instance exists */
bool Binary_Value_Valid_Instance(
    uint32_t object_instance)
{
	return is_object_instance_valid(object_instance, OBJECT_BINARY_VALUE);
}

/* we simply have 0-n object instances.  Yours might be */
/* more complex, and then count how many you have */
unsigned Binary_Value_Count(
    void)
{
	return elf_get_object_count_for_current_device(OBJECT_BINARY_VALUE);
}

/* we simply have 0-n object instances.  Yours might be */
/* more complex, and then you need to return the instance */
/* that correlates to the correct index */
uint32_t Binary_Value_Index_To_Instance(
    unsigned index)
{
	uint32_t instance;
	elf_index_to_object_instance(index, OBJECT_BINARY_VALUE, &instance);
	return instance;
}

static BACNET_BINARY_PV Binary_Value_Present_Value(
    uint32_t object_instance)
{
	return elf_gen_bin_get_present_value(OBJECT_BINARY_VALUE, object_instance);
}

/* note: the object name must be unique within this device */
bool Binary_Value_Object_Name(
    uint32_t object_instance,
	BACNET_CHARACTER_STRING * object_name)
{
	static char text_string[MX_NAME] = ""; /* okay for single thread */
	bool status;

	const char *name;
	name = elf_get_object_name_for_current_device(OBJECT_BINARY_VALUE, object_instance, text_string);
	if (name == NULL)
		return false;
	status = characterstring_init_ansi(object_name, name);
	return status;
}

#ifdef ENLIGHTED_INC
char *Binary_Value_Description(
    uint32_t object_instance)
{
	static char text_string[512] = ""; /* okay for single thread */

	snprintf(text_string, sizeof(text_string), "%s", elf_get_bacnet_description(OBJECT_BINARY_VALUE, object_instance));
	return text_string;
}
#endif /* ENLIGHTED_INC */


/* return apdu len, or BACNET_STATUS_ERROR on error */
int Binary_Value_Read_Property(
    BACNET_READ_PROPERTY_DATA *rpdata)
{
	int len;
	int apdu_len = 0;   /* return value */
	BACNET_BIT_STRING bit_string;
	BACNET_CHARACTER_STRING char_string;
	BACNET_BINARY_PV present_value = BINARY_INACTIVE;
	unsigned i;
	uint8_t *apdu ;
	ts_elf_template_object_t *currentObject;
	BinaryCommandableTypeDescriptor *livedata;

	if ((rpdata == NULL) || (rpdata->application_data == NULL) || (rpdata->application_data_len == 0))
	{
		return 0;
	}

	currentObject = find_template_object_record_current_device(OBJECT_BINARY_VALUE, rpdata->object_instance);
	if (currentObject == NULL)
	{
		panic("Should not be possible");
		return BACNET_STATUS_ERROR;
	}

	livedata = (BinaryCommandableTypeDescriptor *) elf_get_binary_object_live_data_old(currentObject, rpdata->object_instance);
	if (livedata == NULL)
	{
		panic("Should not be possible");
		return BACNET_STATUS_ERROR;
	}

	apdu = rpdata->application_data;
	switch (rpdata->object_property) 
	{
	case PROP_OBJECT_IDENTIFIER:
		apdu_len = encode_application_object_id(&apdu[0], 
			OBJECT_BINARY_VALUE,
			rpdata->object_instance);
		break;
	case PROP_OBJECT_NAME:
		if (Binary_Value_Object_Name(rpdata->object_instance, &char_string))
		{
			apdu_len =
			    encode_application_character_string(&apdu[0], &char_string);
		}
		break;
	case PROP_DESCRIPTION:
		characterstring_init_ansi(&char_string,
			Binary_Value_Description(rpdata->object_instance));
		apdu_len =
		    encode_application_character_string(&apdu[0], &char_string);
		break;

	case PROP_OBJECT_TYPE:
		apdu_len =
		    encode_application_enumerated(&apdu[0], OBJECT_BINARY_VALUE);
		break;

	case PROP_PRESENT_VALUE:
		present_value =
		    Binary_Value_Present_Value(rpdata->object_instance);
		apdu_len = encode_application_enumerated(&apdu[0], present_value);
		break;
	case PROP_STATUS_FLAGS:
	    /* note: see the details in the standard on how to use these */
		bitstring_init(&bit_string);
		bitstring_set_bit(&bit_string, STATUS_FLAG_IN_ALARM, false);
		if (GetReliability(&livedata->binaryTypeDescriptor.objectTypeDescriptor) != RELIABILITY_NO_FAULT_DETECTED)
		{
			bitstring_set_bit(&bit_string, STATUS_FLAG_FAULT, true);
		}
		else 
		{
			bitstring_set_bit(&bit_string, STATUS_FLAG_FAULT, false);
		}
		bitstring_set_bit(&bit_string, STATUS_FLAG_OVERRIDDEN, false);
		bitstring_set_bit(&bit_string,
			STATUS_FLAG_OUT_OF_SERVICE,
			livedata->binaryTypeDescriptor.objectTypeDescriptor.Out_Of_Service);
		apdu_len = encode_application_bitstring(&apdu[0], &bit_string);
		break;
	case PROP_EVENT_STATE:
	    /* note: see the details in the standard on how to use this */
		apdu_len =
		    encode_application_enumerated(&apdu[0], EVENT_STATE_NORMAL);
		break;
	case PROP_RELIABILITY:
		apdu_len = encode_application_enumerated(&apdu[0], 
			GetReliability(&livedata->binaryTypeDescriptor.objectTypeDescriptor));
		break;

	case PROP_OUT_OF_SERVICE:
		apdu_len =
		    encode_application_boolean(&apdu[0],
			livedata->binaryTypeDescriptor.objectTypeDescriptor.Out_Of_Service);
		break;

            
	case PROP_PRIORITY_ARRAY:
	    /* Array element zero is the number of elements in the array */
		if (rpdata->array_index == 0)
		{
			apdu_len = encode_application_unsigned(&apdu[0], BACNET_MAX_PRIORITY);
		}
		else if (rpdata->array_index == BACNET_ARRAY_ALL) 
		{
		    /* if no index was specified, then try to encode the entire list */
		    /* into one packet. */
			for (i = 0; i < BACNET_MAX_PRIORITY; i++)
			{
				if (livedata->priorityAsserted[i])
				{
					len = encode_application_real(&apdu[apdu_len], livedata->priorityArray[i]);
				}
				else
				{
					len = encode_application_null(&apdu[apdu_len]);
				}
				/* add it if we have room */
				if ((apdu_len + len) < MAX_APDU)
				{
					apdu_len += len;
				}
				else
				{
					rpdata->error_class = ERROR_CLASS_SERVICES;
					rpdata->error_code = ERROR_CODE_NO_SPACE_FOR_OBJECT;
					apdu_len = BACNET_STATUS_ERROR;
					break;
				}
			}
		}
		else
		{
			if (rpdata->array_index <= BACNET_MAX_PRIORITY)
			{
				if (!livedata->priorityAsserted[rpdata->array_index-1] )
				{
					apdu_len = encode_application_null(&apdu[0]);
				}
				else
				{
					apdu_len =
					encode_application_real(&apdu[0], livedata->priorityArray[rpdata->array_index -  1]);
				}
			}
			else
			{
				rpdata->error_class = ERROR_CLASS_PROPERTY;
				rpdata->error_code = ERROR_CODE_INVALID_ARRAY_INDEX;
				apdu_len = BACNET_STATUS_ERROR;
			}
		}
		break;
	case PROP_RELINQUISH_DEFAULT:
		apdu_len = encode_application_enumerated(&apdu[0], livedata->relinquishDefault);
		break;

	default:
		rpdata->error_class = ERROR_CLASS_PROPERTY;
		rpdata->error_code = ERROR_CODE_UNKNOWN_PROPERTY;
		apdu_len = BACNET_STATUS_ERROR;
		break;
	}
	/*  only array properties can have array options */
	if ((apdu_len >= 0) && (rpdata->object_property != PROP_PRIORITY_ARRAY) &&
	    (rpdata->array_index != BACNET_ARRAY_ALL)) 
	{
		rpdata->error_class = ERROR_CLASS_PROPERTY;
		rpdata->error_code = ERROR_CODE_PROPERTY_IS_NOT_AN_ARRAY;
		apdu_len = BACNET_STATUS_ERROR;
	}

	return apdu_len;
}

/* returns true if successful */
bool Binary_Value_Write_Property(
    BACNET_WRITE_PROPERTY_DATA *wp_data)
{
	bool status = false; /* return value */
	int len ;
	BACNET_APPLICATION_DATA_VALUE value;
	ts_elf_template_object_t *currentObject;
	BinaryCommandableTypeDescriptor *livedata;

	    /* decode the some of the request */
	len =
	    bacapp_decode_application_data(wp_data->application_data,
		(unsigned int) wp_data->application_data_len,
		&value);
/* FIXME: len < application_data_len: more data? */
	if (len < 0)
	{
	    /* error while decoding - a value larger than we can handle */
		wp_data->error_class = ERROR_CLASS_PROPERTY;
		wp_data->error_code = ERROR_CODE_VALUE_OUT_OF_RANGE;
		return false;
	}

	if ((wp_data->object_property != PROP_PRIORITY_ARRAY) &&
	    (wp_data->object_property != PROP_EVENT_TIME_STAMPS) &&
	    (wp_data->array_index != BACNET_ARRAY_ALL)) 
	{
	    /*  only array properties can have array options */
		wp_data->error_class = ERROR_CLASS_PROPERTY;
		wp_data->error_code = ERROR_CODE_PROPERTY_IS_NOT_AN_ARRAY;
		return false;
	}

	currentObject = find_template_object_record_current_device(OBJECT_BINARY_VALUE, wp_data->object_instance);
	if (currentObject == NULL)
	{
		wp_data->error_class = ERROR_CLASS_OBJECT;
		wp_data->error_code = ERROR_CODE_NO_OBJECTS_OF_SPECIFIED_TYPE;
		return false ;
	}

	livedata = (BinaryCommandableTypeDescriptor *) elf_get_binary_object_live_data_old(currentObject, wp_data->object_instance);
	if (livedata == NULL)
	{
		panic("Should not be possible");
		return false ;
	}

	switch (wp_data->object_property)
	{
	case PROP_PRESENT_VALUE:
		if (value.tag == BACNET_APPLICATION_TAG_ENUMERATED) 
		{
		    /* Command priority 6 is reserved for use by Minimum On/Off
		     algorithm and may not be used for other purposes in any
		     object. */
			status = elf_gen_bin_put_present_value_commandable(
			    OBJECT_BINARY_VALUE, 
				wp_data,
				(value.type.Enumerated) ? BINARY_ACTIVE: BINARY_INACTIVE );
		} 
		else
		{
			status = WPValidateArgType(&value, BACNET_APPLICATION_TAG_NULL, &wp_data->error_class, &wp_data->error_code);
			if (status)
			{
				int rc = elf_gen_bin_relinquish( OBJECT_BINARY_VALUE,
					wp_data);
				if (rc == BACNET_STATUS_OK)
				{
					status = true;
				}
				else
				{
					status = false;
					wp_data->error_class = ERROR_CLASS_PROPERTY;
					wp_data->error_code = ERROR_CODE_VALUE_OUT_OF_RANGE;
				}
			}
		}
		break;

	case PROP_OUT_OF_SERVICE:
		status =
		    WPValidateArgType(&value,
			BACNET_APPLICATION_TAG_BOOLEAN,
			&wp_data->error_class,
			&wp_data->error_code);
		if (status)
		{
			livedata->binaryTypeDescriptor.objectTypeDescriptor.Out_Of_Service = value.type.Boolean;
		}
		break;
	case PROP_OBJECT_IDENTIFIER:
	case PROP_OBJECT_NAME:
	case PROP_DESCRIPTION:
	case PROP_OBJECT_TYPE:
	case PROP_STATUS_FLAGS:
	case PROP_EVENT_STATE:
	case PROP_PRIORITY_ARRAY:
	case PROP_RELINQUISH_DEFAULT:
		wp_data->error_class = ERROR_CLASS_PROPERTY;
		wp_data->error_code = ERROR_CODE_WRITE_ACCESS_DENIED;
		break;
	default:
		wp_data->error_class = ERROR_CLASS_PROPERTY;
		wp_data->error_code = ERROR_CODE_UNKNOWN_PROPERTY;
		break;
	}

	return status;
}
