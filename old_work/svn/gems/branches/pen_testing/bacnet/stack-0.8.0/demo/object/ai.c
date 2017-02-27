/**************************************************************************
 *
 * Copyright (C) 2005 Steve Karg <skarg@users.sourceforge.net>
 * Copyright (C) 2011 Krzysztof Malorny <malornykrzysztof@gmail.com>
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

/* Analog Input Objects customize for your use */

#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>

#include "bacdef.h"
#include "bacdcode.h"
#include "bacenum.h"
#include "config.h"     /* the custom stuff */
#include "handlers.h"
#include "ai.h"
#include "advdebug.h"
#include "elf.h"
#include "elf_gems_api.h"
#include "elf_functions.h"

/* These arrays are used by the ReadPropertyMultiple handler */
static const int Properties_Optional[] = {
        PROP_DESCRIPTION,
        PROP_RELIABILITY,
        PROP_COV_INCREMENT,
#if defined(INTRINSIC_REPORTING)
        PROP_TIME_DELAY,
        PROP_NOTIFICATION_CLASS,
        PROP_HIGH_LIMIT,
        PROP_LOW_LIMIT,
        PROP_DEADBAND,
        PROP_LIMIT_ENABLE,
        PROP_EVENT_ENABLE,
        PROP_ACKED_TRANSITIONS,
        PROP_NOTIFY_TYPE,
        PROP_EVENT_TIME_STAMPS,
#endif
        -1 };

static const int Properties_Proprietary[] = {
        9997,
        9998,
        9999,
        -1 };

void Analog_Input_Property_Lists(const int **pRequired, const int **pOptional, const int **pProprietary)
{
    if (pRequired)
        *pRequired = property_list_required(OBJECT_ANALOG_INPUT);
    if (pOptional)
        *pOptional = Properties_Optional;
    if (pProprietary)
        *pProprietary = Properties_Proprietary;

    return;
}


void Analog_Input_Init(
    void)
{
}


/* we simply have 0-n object instances.  Yours might be */
/* more complex, and then you need validate that the */
/* given instance exists */
bool Analog_Input_Valid_Instance(uint32_t object_instance)
{
    return is_object_instance_valid(object_instance, OBJECT_ANALOG_INPUT);
}

/* we simply have 0-n object instances.  Yours might be */
/* more complex, and then count how many you have */
unsigned Analog_Input_Count(void)
{
	return elf_get_object_count_for_current_device(OBJECT_ANALOG_INPUT);
}

/* we simply have 0-n object instances.  Yours might be */
/* more complex, and then you need to return the instance */
/* that correlates to the correct index */
uint32_t Analog_Input_Index_To_Instance(unsigned index)
{
    uint32_t instance;
    elf_index_to_object_instance(index, OBJECT_ANALOG_INPUT, &instance);
    return instance;
}

//float Analog_Input_Present_Value(uint32_t object_instance)
//{
    //return elf_get_ai_present_value(object_instance);
//}


void Analog_Input_Present_Value_Set(uint32_t object_instance, float value)
{
    // unsigned int index = 0;

    // index = Analog_Input_Instance_To_Index(object_instance);
// todo    if (index < MAX_ANALOG_INPUTS) {
//        Analog_Input_COV_Detect(index, value);
// todo        AI_Descr[index].Present_Value = value;
//    }
}

bool Analog_Input_Object_Name(uint32_t object_instance, BACNET_CHARACTER_STRING * object_name)
{
    static char text_string[MX_NAME] = ""; /* okay for single thread */
    bool status = false;

    const char *name;
	name = elf_get_object_name_for_current_device(OBJECT_ANALOG_INPUT, object_instance, text_string);
    if (name == NULL)
        return false;
    status = characterstring_init_ansi(object_name, name);
    return status;
}

#ifdef ENLIGHTED_INC
char *Analog_Input_Description(uint32_t object_instance)
{
    static char text_string[512] = ""; /* okay for single thread */

    snprintf(text_string, sizeof(text_string), "%s", elf_get_bacnet_description( OBJECT_ANALOG_INPUT, object_instance));
    return text_string;

}
#endif /* ENLIGHTED_INC */

bool Analog_Input_Change_Of_Value(uint32_t object_instance)
{
    // unsigned index = 0;
    bool changed = false;

    // index = Analog_Input_Instance_To_Index(object_instance);
//    if (index < MAX_ANALOG_INPUTS) {
// todonext         changed = AI_Descr[index].Changed;
//    }

    return changed;
}

void Analog_Input_Change_Of_Value_Clear(uint32_t object_instance)
{
    // unsigned index = 0;

    // index = Analog_Input_Instance_To_Index(object_instance);
//    if (index < MAX_ANALOG_INPUTS) {
// todonext        AI_Descr[index].Changed = false;
//    }

}

/* return apdu length, or BACNET_STATUS_ERROR on error */
/* assumption - object has already exists */
int Analog_Input_Read_Property(BACNET_READ_PROPERTY_DATA * rpdata)
{
    int apdu_len = 0; /* return value */
    BACNET_BIT_STRING bit_string;
    BACNET_CHARACTER_STRING char_string;
    const ts_elf_template_object_t *currentObject ;
    uint8_t *apdu = NULL;
    const int *pRequired = NULL, *pOptional = NULL, *pProprietary = NULL;
    AnalogObjectTypeDescriptor *livedata ;

    if ((rpdata == NULL) || (rpdata->application_data == NULL) || (rpdata->application_data_len == 0))
    {
        return 0;
    }

	currentObject = find_template_object_record_current_device(OBJECT_ANALOG_INPUT, rpdata->object_instance);
    if (currentObject == NULL)
        {
        panic("Should not be possible");
        return BACNET_STATUS_ERROR;
        }

	livedata = (AnalogObjectTypeDescriptor *) elf_get_analog_object_live_data_old(currentObject, rpdata->object_instance);
    if (livedata == NULL)
        {
        panic("Should not be possible");
        return BACNET_STATUS_ERROR;
        }

    apdu = rpdata->application_data;
    switch (rpdata->object_property)
    {
    case PROP_OBJECT_IDENTIFIER:
        apdu_len = encode_application_object_id(&apdu[0], OBJECT_ANALOG_INPUT, rpdata->object_instance);
        break;

    case PROP_OBJECT_NAME:
        if (Analog_Input_Object_Name(rpdata->object_instance, &char_string))
        {
            apdu_len = encode_application_character_string(&apdu[0], &char_string);
        }
        break;
    case PROP_DESCRIPTION:
        characterstring_init_ansi(&char_string, Analog_Input_Description(rpdata->object_instance));
        apdu_len = encode_application_character_string(&apdu[0], &char_string);
        break;

    case PROP_OBJECT_TYPE:
        apdu_len = encode_application_enumerated(&apdu[0], OBJECT_ANALOG_INPUT);
        break;

    case PROP_PRESENT_VALUE:
	    apdu_len = encode_application_real(&apdu[0], livedata->presentValue); // Analog_Input_Present_Value(rpdata->object_instance));
        break;

    case PROP_STATUS_FLAGS:
        bitstring_init(&bit_string);
        bitstring_set_bit(&bit_string, STATUS_FLAG_IN_ALARM, false);
	    if (GetReliability(&livedata->objectTypeDescriptor) != RELIABILITY_NO_FAULT_DETECTED)
	    {
		    bitstring_set_bit(&bit_string, STATUS_FLAG_FAULT, true );
	    }
	    else 
	    {
		    bitstring_set_bit(&bit_string, STATUS_FLAG_FAULT, false);
	    }
        bitstring_set_bit(&bit_string, STATUS_FLAG_OVERRIDDEN, false);
        bitstring_set_bit(&bit_string, STATUS_FLAG_OUT_OF_SERVICE,
                          livedata->objectTypeDescriptor.Out_Of_Service );

        apdu_len = encode_application_bitstring(&apdu[0], &bit_string);
        break;

    case PROP_EVENT_STATE:
        apdu_len = encode_application_enumerated(&apdu[0], EVENT_STATE_NORMAL);
        break;

    case PROP_RELIABILITY:
	    apdu_len = encode_application_enumerated(&apdu[0], GetReliability(&livedata->objectTypeDescriptor));
        break;

    case PROP_OUT_OF_SERVICE:
        apdu_len =
            encode_application_boolean(&apdu[0],
                    livedata->objectTypeDescriptor.Out_Of_Service);
        break;

    case PROP_UNITS:
#ifdef ENLIGHTED_INC
        apdu_len = encode_application_enumerated(&apdu[0], elf_get_bacnet_units(OBJECT_ANALOG_INPUT, rpdata->object_instance));
#else /* ENLIGHTED_INC */
        apdu_len =
        encode_application_enumerated(&apdu[0], CurrentAI->Units);
#endif /* ENLIGHTED_INC */
        break;

//    case PROP_COV_INCREMENT:
//        apdu_len = encode_application_real(&apdu[0], CurrentAI->COV_Increment);
//        break;

#if defined(INTRINSIC_REPORTING)
        case PROP_TIME_DELAY:
        apdu_len =
        encode_application_unsigned(&apdu[0], CurrentAI->Time_Delay);
        break;

        case PROP_NOTIFICATION_CLASS:
        apdu_len =
        encode_application_unsigned(&apdu[0],
                CurrentAI->Notification_Class);
        break;

        case PROP_HIGH_LIMIT:
        apdu_len =
        encode_application_real(&apdu[0], CurrentAI->High_Limit);
        break;

        case PROP_LOW_LIMIT:
        apdu_len = encode_application_real(&apdu[0], CurrentAI->Low_Limit);
        break;

        case PROP_DEADBAND:
        apdu_len = encode_application_real(&apdu[0], CurrentAI->Deadband);
        break;

        case PROP_LIMIT_ENABLE:
        bitstring_init(&bit_string);
        bitstring_set_bit(&bit_string, 0,
                (CurrentAI->
                        Limit_Enable & EVENT_LOW_LIMIT_ENABLE) ? true : false);
        bitstring_set_bit(&bit_string, 1,
                (CurrentAI->
                        Limit_Enable & EVENT_HIGH_LIMIT_ENABLE) ? true : false);

        apdu_len = encode_application_bitstring(&apdu[0], &bit_string);
        break;

        case PROP_EVENT_ENABLE:
        bitstring_init(&bit_string);
        bitstring_set_bit(&bit_string, TRANSITION_TO_OFFNORMAL,
                (CurrentAI->
                        Event_Enable & EVENT_ENABLE_TO_OFFNORMAL) ? true : false);
        bitstring_set_bit(&bit_string, TRANSITION_TO_FAULT,
                (CurrentAI->
                        Event_Enable & EVENT_ENABLE_TO_FAULT) ? true : false);
        bitstring_set_bit(&bit_string, TRANSITION_TO_NORMAL,
                (CurrentAI->
                        Event_Enable & EVENT_ENABLE_TO_NORMAL) ? true : false);

        apdu_len = encode_application_bitstring(&apdu[0], &bit_string);
        break;

        case PROP_ACKED_TRANSITIONS:
        bitstring_init(&bit_string);
        bitstring_set_bit(&bit_string, TRANSITION_TO_OFFNORMAL,
                CurrentAI->Acked_Transitions[TRANSITION_TO_OFFNORMAL].
                bIsAcked);
        bitstring_set_bit(&bit_string, TRANSITION_TO_FAULT,
                CurrentAI->Acked_Transitions[TRANSITION_TO_FAULT].bIsAcked);
        bitstring_set_bit(&bit_string, TRANSITION_TO_NORMAL,
                CurrentAI->Acked_Transitions[TRANSITION_TO_NORMAL].bIsAcked);

        apdu_len = encode_application_bitstring(&apdu[0], &bit_string);
        break;

        case PROP_NOTIFY_TYPE:
        apdu_len =
        encode_application_enumerated(&apdu[0],
                CurrentAI->Notify_Type ? NOTIFY_EVENT : NOTIFY_ALARM);
        break;

        case PROP_EVENT_TIME_STAMPS:
        /* Array element zero is the number of elements in the array */
        if (rpdata->array_index == 0)
        apdu_len =
        encode_application_unsigned(&apdu[0],
                MAX_BACNET_EVENT_TRANSITION);
        /* if no index was specified, then try to encode the entire list */
        /* into one packet. */
        else if (rpdata->array_index == BACNET_ARRAY_ALL)
        {
            for (i = 0; i < MAX_BACNET_EVENT_TRANSITION; i++)
            {   ;
                len =
                encode_opening_tag(&apdu[apdu_len],
                        TIME_STAMP_DATETIME);
                len +=
                encode_application_date(&apdu[apdu_len + len],
                        &CurrentAI->Event_Time_Stamps[i].date);
                len +=
                encode_application_time(&apdu[apdu_len + len],
                        &CurrentAI->Event_Time_Stamps[i].time);
                len +=
                encode_closing_tag(&apdu[apdu_len + len],
                        TIME_STAMP_DATETIME);

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
        else if (rpdata->array_index <= MAX_BACNET_EVENT_TRANSITION)
        {
            apdu_len =
            encode_opening_tag(&apdu[apdu_len], TIME_STAMP_DATETIME);
            apdu_len +=
            encode_application_date(&apdu[apdu_len],
                    &CurrentAI->Event_Time_Stamps[rpdata->array_index].date);
            apdu_len +=
            encode_application_time(&apdu[apdu_len],
                    &CurrentAI->Event_Time_Stamps[rpdata->array_index].time);
            apdu_len +=
            encode_closing_tag(&apdu[apdu_len], TIME_STAMP_DATETIME);
        }
        else
        {
            rpdata->error_class = ERROR_CLASS_PROPERTY;
            rpdata->error_code = ERROR_CODE_INVALID_ARRAY_INDEX;
            apdu_len = BACNET_STATUS_ERROR;
        }
        break;
#endif
    case PROP_PROPERTY_LIST:
        Analog_Input_Property_Lists(&pRequired, &pOptional, &pProprietary);
        apdu_len = property_list_encode(rpdata, pRequired, pOptional, pProprietary);
        break;
    default:
        rpdata->error_class = ERROR_CLASS_PROPERTY;
        rpdata->error_code = ERROR_CODE_UNKNOWN_PROPERTY;
        apdu_len = BACNET_STATUS_ERROR;
        break;
    }
    /*  only array properties can have array options */
    if ((apdu_len >= 0) && (rpdata->object_property != PROP_EVENT_TIME_STAMPS) && (rpdata->object_property != PROP_PROPERTY_LIST) && (rpdata->array_index != BACNET_ARRAY_ALL))
    {
        rpdata->error_class = ERROR_CLASS_PROPERTY;
        rpdata->error_code = ERROR_CODE_PROPERTY_IS_NOT_AN_ARRAY;
        apdu_len = BACNET_STATUS_ERROR;
    }

    return apdu_len;
}

/* returns true if successful */
bool Analog_Input_Write_Property(BACNET_WRITE_PROPERTY_DATA * wp_data)
{
    bool status = false; /* return value */
    // unsigned int object_index = 0;
    int len = 0;
    BACNET_APPLICATION_DATA_VALUE value;
    ts_elf_template_object_t *currentObject;
    AnalogObjectTypeDescriptor *livedata ;

    /* decode the some of the request */
    len = bacapp_decode_application_data(wp_data->application_data, wp_data->application_data_len, &value);
    /* FIXME: len < application_data_len: more data? */
    if (len < 0)
    {
        /* error while decoding - a value larger than we can handle */
        wp_data->error_class = ERROR_CLASS_PROPERTY;
        wp_data->error_code = ERROR_CODE_VALUE_OUT_OF_RANGE;
        return false;
    }
    /*  only array properties can have array options */
    if ((wp_data->object_property != PROP_EVENT_TIME_STAMPS) && (wp_data->object_property != PROP_PROPERTY_LIST) && (wp_data->array_index != BACNET_ARRAY_ALL))
    {
        wp_data->error_class = ERROR_CLASS_PROPERTY;
        wp_data->error_code = ERROR_CODE_PROPERTY_IS_NOT_AN_ARRAY;
        return false;
    }

	currentObject = find_template_object_record_current_device(OBJECT_ANALOG_INPUT, wp_data->object_instance);
    if (currentObject == NULL)
    {
        wp_data->error_class = ERROR_CLASS_OBJECT;
        wp_data->error_code = ERROR_CODE_NO_OBJECTS_OF_SPECIFIED_TYPE;
        return false;
    }

	livedata = (AnalogObjectTypeDescriptor *) elf_get_analog_object_live_data_old(currentObject, wp_data->object_instance);
    if (livedata == NULL)
        {
        panic("Should not be possible");
        return false;
        }

    switch (wp_data->object_property)
    {
    case PROP_PRESENT_VALUE:
        status = WPValidateArgType(&value, BACNET_APPLICATION_TAG_REAL, &wp_data->error_class, &wp_data->error_code);

//            if (status) {
// todo                if (CurrentAI->Out_Of_Service == true) {
//                    Analog_Input_Present_Value_Set(wp_data->object_instance,
//                        value.type.Real);
//                } else {
        wp_data->error_class = ERROR_CLASS_PROPERTY;
        wp_data->error_code = ERROR_CODE_WRITE_ACCESS_DENIED;
        status = false;
//                }
        //}
        break;

    case PROP_OUT_OF_SERVICE:
        status =
            WPValidateArgType(&value, BACNET_APPLICATION_TAG_BOOLEAN,
                              &wp_data->error_class, &wp_data->error_code);
        if (status)
        {
            livedata->objectTypeDescriptor.Out_Of_Service = value.type.Boolean;
        }
        break;

    case PROP_OBJECT_IDENTIFIER:
    case PROP_OBJECT_NAME:
    case PROP_DESCRIPTION:
    case PROP_OBJECT_TYPE:
    case PROP_STATUS_FLAGS:
    case PROP_EVENT_STATE:
    case PROP_UNITS:
    case PROP_RELIABILITY:
    case PROP_PROPERTY_LIST:
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

