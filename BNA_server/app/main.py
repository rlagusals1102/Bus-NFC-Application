from fastapi import FastAPI
from routes import bus_info, bus_gps, bus_finder, bus_list
from utils.keys import SERVICE_KEY
app = FastAPI()

app.include_router(bus_info.router)
app.include_router(bus_gps.router)
app.include_router(bus_finder.router)
app.include_router(bus_list.router)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
