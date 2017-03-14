package kankan.wheel.demo.extended;

public class CityDTO {
    public String area;
    public String areaID;      //该市辖区id
    public String father;      //上一级id
    public int id;
    public String areaType;     //1,内地，2港澳台，3海外

    public CityDTO()
    {
    }

    @Override
    public String toString() {
        return  area;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CityDTO cityDTO = (CityDTO) o;

        return id == cityDTO.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
