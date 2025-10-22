#!/bin/bash
# recreate-h2-db.sh
# Script to recreate the H2 database from scratch for development
# This will delete existing database files and recreate with fresh schema and data

set -e

echo "=================================================="
echo "  Recreating H2 Database for Development"
echo "=================================================="
echo ""

# Get the directory where the script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Define database directory and files
DATA_DIR="./data"
DB_FILES=(
    "$DATA_DIR/agendadb.mv.db"
    "$DATA_DIR/agendadb.trace.db"
)

echo "Step 1: Checking for existing H2 database files..."
DB_EXISTS=0
for DB_FILE in "${DB_FILES[@]}"; do
    if [ -f "$DB_FILE" ]; then
        echo "  Found: $DB_FILE"
        DB_EXISTS=1
    fi
done

if [ $DB_EXISTS -eq 0 ]; then
    echo "  No existing database files found."
else
    echo ""
    echo "Step 2: Deleting existing database files..."
    for DB_FILE in "${DB_FILES[@]}"; do
        if [ -f "$DB_FILE" ]; then
            rm -f "$DB_FILE"
            echo "  Deleted: $DB_FILE"
        fi
    done
    echo "  Database files removed successfully."
fi

echo ""
echo "Step 3: Setting up application to recreate database..."
echo "  - Using spring.jpa.hibernate.ddl-auto=create"
echo "  - Using app.reload-data=true"
echo ""

# Create temporary properties override
TEMP_PROPS="recreate-db-temp.properties"
cat > "$TEMP_PROPS" << 'EOF'
# Temporary properties to recreate database from scratch
spring.jpa.hibernate.ddl-auto=create
app.reload-data=true
EOF

echo "Step 4: Starting application to recreate database and load initial data..."
echo "  (This will compile and run the Spring Boot application)"
echo ""

# Run with the temporary properties as additional config
if [ -f "$TEMP_PROPS" ]; then
    mvn clean spring-boot:run -Dspring-boot.run.arguments="--spring.config.additional-location=file:./$TEMP_PROPS"
    EXIT_CODE=$?
    
    # Cleanup temporary file
    rm -f "$TEMP_PROPS"
    
    if [ $EXIT_CODE -eq 0 ]; then
        echo ""
        echo "=================================================="
        echo "  H2 Database recreated successfully!"
        echo "=================================================="
        echo ""
        echo "You can now run the application normally using:"
        echo "  ./run-dev.bat (Windows)"
        echo "  mvn spring-boot:run -Dspring-boot.run.profiles=dev (Linux/Mac)"
        echo ""
    else
        echo ""
        echo "ERROR: Application failed to start. Exit code: $EXIT_CODE"
        echo "Please check the logs above for details."
        exit $EXIT_CODE
    fi
else
    echo "ERROR: Could not create temporary properties file"
    exit 1
fi
