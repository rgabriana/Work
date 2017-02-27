# createGateway.py
import sys, psycopg2 

def createGateway(floor_id):

    print ("Creating Gateway for Floor Id: " + str(floor_id))

    connection = psycopg2.connect('dbname=ems user=postgres password=postgres')
    mark = connection.cursor() 

    psycopg2.extensions.register_type(psycopg2.extensions.UNICODE, mark)

    statement = ' Select b.id as building_id, campus.id as campus_id, company.id as company_id \
              from floor f , building b, campus campus, company company \
              where \
              f.building_id = b.id and b.campus_id = campus.id and \
              campus.company_id = company.id and f.id = (%s)'
    
    mark.execute(statement, floor_id) 
    record = mark.fetchone()

    building_id = record[0]
    campus_id = record[1]
    company_id = record[2]
              
    mac_address = '68:54:f5:00:01:7f'
    ip_address = '127.0.0.1'
    subnet_mask = '255.255.0.0'
    commissioned = 't'
    comm_type = 3
    empty_str = ""
    network_id = 6854
    channel_no = 4
    port = 8085
    status = 't'
    wireless_enctype = 1
    wireless_enckey = 'enLightedWorknow'
    wireless_radiorate = 2
    
    
    insert_gw_sql = 'insert into gateway (id, gateway_name, gateway_type, ip_address, floor_id, campus_id, \
                    building_id, port, mac_address, subnet_mask, snap_address, commissioned, status, \
                    wireless_networkid, wireless_enctype, wireless_enckey, wireless_radiorate, channel) \
                    values ( \
                    %s, %s, %s, %s, %s, %s, \
                    %s, %s, %s, %s, %s, \
                    %s, %s, %s, %s, %s, \
                    %s, %s)'
                    
    data = (1,'GW6854f5', 1, ip_address, floor_id, campus_id,
            building_id, port, mac_address, subnet_mask, '6854f', commissioned, status, 
            network_id, wireless_enctype, wireless_enckey, wireless_radiorate, channel_no)
    
    mark.execute(insert_gw_sql, data)
        
    connection.commit()
    mark.close()
    connection.close()    

    return
