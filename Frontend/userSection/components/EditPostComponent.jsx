import React, { useState , useEffect} from "react";
import { useNavigate } from "react-router-dom";
import LoadingOverlay from "../../loadingComponents/Loading";
import axios from "axios";

export default function UpdatePostForm({onBack, postId}) {
  const navigate = useNavigate();
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [latitude, setLatitude] = useState("");
  const [longitude, setLongitude] = useState("");
  const [address, setAddress] = useState("");
  const [city, setCity] = useState("");
  const [state, setState] = useState("");
  const [zip, setZip] = useState("");
  const [msg, updateMsg] = useState(null);
  const [processing, setProcessing] = useState(false);
  const [location, setLocation] = useState("");
  const [authenticated, setAuthenticated] = useState(false);
  const [id, setId] = useState(-1);

  useEffect(() => {
      if (localStorage.getItem("user")!==null){
        setAuthenticated(true);
        fetchFullPostData();
      } else{
        setAuthenticated(false);
        navigate("/user/login");
      }
    }, [navigate]);

  const fetchFullPostData = async()=>{
    try{
      const user = JSON.parse(localStorage.getItem("user"));
      setProcessing(true);
      const response=await axios.get(`http://localhost:8080/posts/${postId}`,
        {
          headers:{
            "Authorization": "Bearer "+user.token,
            "Content-Type": "application/json"
          }
        }
      );
      if(response.status===200){
        setTitle(response.data.object.postTitle);
        setDescription(response.data.object.postDescription);
        setAddress(response.data.object.street);
        setCity(response.data.object.city);
        setState(response.data.object.state);
        setZip(response.data.object.postalCode);
        setId(response.data.object.postId);
        setProcessing(false);
      }else if(response.status===202){
        setProcessing(false);
        updateMsg(response.data.msg);
      }
    }catch(exception){
      console.log(exception);
      setProcessing(false);
      updateMsg(exception?.response?.data?.msg || exception.message );
      navigate("/user/login");
    }
  };

  const fetchLocation = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setLocation(`Latitude: ${position.coords.latitude}, Longitude: ${position.coords.longitude}`);
          setLatitude(position.coords.latitude);
          setLongitude(position.coords.longitude);
        },
        (error) => {
          console.error("Error fetching location:", error);
          alert("Unable to fetch location. Please allow location access.");
        }
      );
    } else {
      alert("Geolocation is not supported by your browser.");
    }
  };

  const handleUpdate = async() => {
    setProcessing(true);
    try{
      const user=JSON.parse(localStorage.getItem("user"));
      const editPostData={
        "postId":id,
        "postTitle":title,
        "postDescription":description,
        "latitude":latitude,
        "longitude":longitude,
        "street":address,
        "city":city,
        "state":state,
        "postalCode":zip
      };
      const response = await axios.post("http://localhost:8080/post/edit",editPostData,{
        headers:{
            "Authorization": "Bearer "+user.token,
            "Content-Type": "application/json"
          }
      });
      if(response.status===200 && response.data.object){
        setProcessing(false);
        updateMsg("Post updated.");
      }else if(response.status===202){
        setProcessing(false);
        updateMsg(response.data.msg);
      }
    }catch(exception){
      setProcessing(false);
      console.log(exception);
      updateMsg("Token expired login again!");
      if(exception.response && (exception.response.status===401 || exception.response.status===403))navigate("/user/login");
    }
  };

  if (!authenticated) return null;

  return (
    <div style={styles.container}>
      <div onClick={() => {onBack();}} style={styles.backArrow}>
      ← Back
      </div>
      {processing ? <div>Loading...</div> : ""}
      <h2 style={styles.heading}>Update Your Post</h2>
      {msg && (
        <div style={styles.alertDiv}>
          <h3 style={styles.alertText}>{msg}</h3>
        </div>
      )}
      <form style={styles.form} onSubmit={(e) => { e.preventDefault(); handleUpdate(); }}>
        <input
          type="text"
          placeholder="Enter Post Title"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          style={styles.input}
          required
        />
        <textarea
          placeholder="Enter Post Description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          style={styles.textarea}
          required
        />
        <div style={styles.locationSection}>
          <button type="button" onClick={fetchLocation} style={styles.locationButton}>
            Update Location
          </button>
          <input
            type="text"
            value={location}
            onChange={(e) => setLocation(e.target.value)}
            placeholder="Current Location"
            style={styles.input}
            readOnly
          />
        </div>
        <div style={styles.addressSection}>
          <input
            type="text"
            placeholder="Address"
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            style={styles.input}
            required
          />
          <input
            type="text"
            placeholder="City"
            value={city}
            onChange={(e) => setCity(e.target.value)}
            style={styles.input}
            required
          />
          <input
            type="text"
            placeholder="State"
            value={state}
            onChange={(e) => setState(e.target.value)}
            style={styles.input}
            required
          />
          <input
            type="text"
            placeholder="ZIP Code"
            value={zip}
            onChange={(e) => setZip(e.target.value)}
            style={styles.input}
            required
          />
        </div>
        <button type="submit" style={styles.submitButton}>
          Update Post
        </button>
      </form>
    </div>
  );
}

const styles = {
  backArrow: {
    cursor: "pointer",
    fontSize: "1.5rem",
    color: "#004080",
    fontWeight: "bold",
    marginBottom: "-90px",
    display: "inline-block",
    backgroundColor: "#e3f2fd",
    padding: "0.4rem 0.8rem",
    borderRadius: "6px",
    boxShadow: "0 2px 5px rgba(0, 0, 0, 0.1)",
    transition: "background-color 0.2s ease-in-out",
    width: "fit-content",
    marginBottom:"1px",
    marginRight:"650px"
  },
  alertDiv: { textAlign: "center" },
  alertText: {
    backgroundColor: "rgb(255, 64, 57)",
    padding: "2px",
    color: "white",
    borderRadius: "8px",
    width: "100%",
    marginBottom: "15px",
    margin: "auto",
    marginTop: "-10px",
  },
  container: {
    backgroundColor: "#f0f8ff",
    minHeight: "100vh",
    padding: "2rem",
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
  },
  heading: {
    color: "#0056b3",
    marginBottom: "1.5rem",
  },
  form: {
    backgroundColor: "#ffffff",
    padding: "2rem",
    borderRadius: "12px",
    boxShadow: "0 4px 8px rgba(0,0,0,0.1)",
    width: "100%",
    maxWidth: "700px",
    display: "flex",
    flexDirection: "column",
    gap: "1.5rem",
  },
  input: {
    padding: "0.8rem",
    borderRadius: "8px",
    border: "1px solid #007bff",
    outline: "none",
    fontSize: "1rem",
  },
  textarea: {
    padding: "0.8rem",
    borderRadius: "8px",
    border: "1px solid #007bff",
    outline: "none",
    fontSize: "1rem",
    minHeight: "100px",
    resize: "vertical",
  },
  imageSectionContainer: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(100px, 1fr))",
    gap: "1rem",
  },
  imageSection: {
    backgroundColor: "#e6f0ff",
    border: "2px dashed #007bff",
    borderRadius: "10px",
    height: "120px",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    position: "relative",
    overflow: "hidden",
  },
  uploadLabel: {
    width: "100%",
    height: "100%",
    cursor: "pointer",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    flexDirection: "column",
    color: "#007bff",
    fontWeight: "bold",
  },
  placeholderText: {
    textAlign: "center",
  },
  hiddenInput: {
    display: "none",
  },
  previewImage: {
    width: "100%",
    height: "100%",
    objectFit: "cover",
    borderRadius: "8px",
  },
  locationSection: {
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    gap: "0.5rem",
  },
  locationButton: {
    backgroundColor: "#007bff",
    color: "#ffffff",
    padding: "0.7rem 1.5rem",
    border: "none",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "bold",
    transition: "background-color 0.3s",
  },
  addressSection: {
    display: "flex",
    flexDirection: "column",
    gap: "1rem",
  },
  submitButton: {
    marginTop: "1rem",
    backgroundColor: "#0056b3",
    color: "#ffffff",
    padding: "0.8rem",
    border: "none",
    borderRadius: "10px",
    fontSize: "1rem",
    cursor: "pointer",
    fontWeight: "bold",
    transition: "background-color 0.3s",
  },
};


