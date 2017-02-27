import sys, psycopg2 

import createGateway


floor_id = sys.argv[1];
no_of_fixture = sys.argv[2];
print ("Creating fixtures for Floor: " + floor_id)
print ("Number of Fixtures: " + no_of_fixture)


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

print("Building id: " + str(building_id)) 
print("Campus id: " + str(campus_id)) 
print("Company id: " + str(company_id))

'Create the gateway'
createGateway.createGateway(floor_id)

connection.commit()
mark.close()
connection.close()