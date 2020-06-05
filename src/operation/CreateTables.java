package src.operation;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


public class CreateTables {
	public String tablePrefix;
	Connection con;
	public CreateTables(Connection con,String tablePrefix){
		this.con=con;
		this.tablePrefix=tablePrefix;
		iniDataBase();
	}
	private void iniDataBase() {		
	     try {
	    	 System.out.println("Success eastablishing the Connection...");
			Statement stm1=con.createStatement();
			
			/*
			 * *Normal_tr	
			    Event	Normal event 	Char(1)
				Time 	Time 	Double
				SN	Source  node 	Integer
				DN	Destination  node	Integer
				PN	Packet name	Varchar(4)
				PS	Packet size 	Integer
				Flags	Flags	Varchar(7)
				FI	Flow id	Integer
				SAN	Source  address(node)	Integer
				SAP	Source  address(port)	Integer
				DAN	Destination  address(node)	Integer
				DAP	Destination  address(port)	Integer
				SeqN	Sequence  number	Integer
				UPI	Unique packet id	Integer
				TCP_AN	Ack  number	Integer
				TCP_Flags	Flags	Integer
				TCP_HL	Header  length	Integer
				TCP_SAL	Socket  address  length	Integer
				Satellite_SLat	Source  latitude	Double
				Satellite_Slon	Source longitude	Double
				Satellite_DLat	Destination  latitude	Double
				Satellite_DLon	Destination  Longitude	Double

			 */			
			
			//create tables for normal trace
			stm1.execute("create table "+tablePrefix+"normal_tr ("+"Event char(1) not null,"+
					"Time double not null,"+
					"SN integer not null,"+
					"DN integer,"+
					"PN varchar(7),"+
					"PS integer,"+
					"Flags varchar(7),"+
					"FI integer,"+
					"SAN integer,"+
					"SAP integer,"+
					"DAN integer,"+
					"DAP integer,"+
					"SeqN integer,"+
					"UPI integer,"+
					"TCP_AN integer,"+
					"TCP_Flags integer,"+
					"TCP_HL integer,"+
					"TCP_SAL integer,"+
					"Satellite_SLat double,"+
					"Satellite_SLon double,"+
					"Satellite_DLat double,"+
					"Satellite_DLon double,"+
					"Name varchar(4)"+")");
		
//			stm1.execute("drop table "+tablePrefix+"normal_tr");			
			
			/*
			 * *
Old_wireless_tr
 	Event 	Event 	Char(1)
	Time	Time 	Double
	NI	Node  id	Integer
	TN	Trace  name	Varchar(3)
	Reason	Reason	Varchar(7)
	EI	Event  identifier	Integer
	PT	Packet  type 	Varchar(7)
	PS	Packet  size	Integer
	TTSD	Time to send data	Integer
	DMA	Destination  mac address	Integer
	SMA	Source mac address	Integer
	Type 	Tpye	Integer
	ARP_ROR	Request or reply	Varchar(7)
	ARP_SMA	Source mac address	Integer
	ARP_SA	Source address	Integer
	ARP_DMA	Destination mac address	Integer
	ARP_DA	Destination address	Integer
	IP_SIA	Source ip address	Integer
	IP_SPN	Source port number	Integer
	IP_DIA	Destination ip address	Integer
	IP_DPN	Destination port number	Integer
	IP_TTL	TTL value 	Integer
	IP_NHA	Next hop address,If any	Integer
	DSR_NONT	Number of nodes traversed	Integer
	DSR_RReqF	Routing request flag	Integer
	DSR_RReqSN	Route request sequence number	Integer
	DSR_RRepF	Routing reply flag	Integer
	DSR_RRepSN	Route reply sequence number	Integer
	DSR_RL	Reply length	Integer
	DSR_SOSR	Source of source routing 	Integer
	DSR_DOSR	Destination of source routing 	integer
	DSR_ERF	Error report flag	Integer
	DSR_NOE	Number of errors	Integer
	DSR_RTW	Report to whom 	Integer
	DSR_LEF	Link error from 	Integer
	DSR_LET	Link error to 	Integer
	AODV_ReqType	Type	Integer
	AODV_ReqHC	Hop count 	Integer
	AODV_ReqBI	Broadcast id	Integer
	AODV_ReqDestination	Destinaion	Integer
	AODV_ReqDSN	Destination sequence number	Integer
	AODV_ReqSource	Source 	Integer
	AODV_ReqSSN	Source sequence number	integer
	AODV_Type	Type 	Intger
	AODV_HC	Hop count	Integer
	AODV_Destination	Destination	Integer
	AODV_DSN	Destination sequence number	Integer
	AODV_Lifetime	Lifetime	Integer
	AODV_Operation	Operation	Varchar(5)
	TORA_QueType	Type	Integer
	TORA_QueDestination	Destination	Integer
	TORA_UpdType 	Type	Integer
	TORA_UpdDestination	Destination	Integer
	TORA_UpdTau	Tau	Double
	TORA_UpdOid	Oid	Integer
	TORA_UpdR	R	Integer
	TORA_UpdDelta	Delta	Integer
	TORA_UpdID	ID	Integer
	TORA_CleType	Type 	Integer
	TORA_CleDestination	Destination 	Integer 
	TORA_CleTau	Tau	Double
	TORA_CleOid	Oid	Integer
	TCP_SN	Sequence number	Integer
	TCP_AN	Acknowledgment number	Integer
	TCP_NOTPWF	Number of times packet was forwarded	Integer
	TCP_ONOF	Optimal number of Forwards	Integer
	CBR_SN	Sequence Number	Integer
	CBR_NOTPWF`	Number of times packet was forwarded	Integer
	CBR_ONOF	Optimal number of Forwards	Integer
	IMEP_AF	Acknowledgment flag	Char(1)
	IMEP_HF	Hello  flag	Char(1)
	IMEP_OF	Object flag	Char(1)
	IMEP_Length	Length	integer

			 */
			
			//create tables for old wireless trace
			stm1.execute("create table "+tablePrefix+"old_wireless_tr ("+"Event char(1) not null,"+
					"Time double not null,"+
					"NI integer not null,"+
					"TN varchar(3),"+
					"Reason varchar(7),"+
					"EI integer,"+
					"PT varchar(7),"+
					"PS integer,"+
					"TTSD integer,"+
					"DMA integer,"+
					"SMA integer,"+
					"Type integer,"+
					"ARP_ROR varchar(7),"+
					"ARP_SMA integer,"+
					"ARP_SA integer,"+
					"ARP_DMA integer,"+
					"ARP_DA integer,"+
					"IP_SIA integer,"+
					"IP_SPN integer,"+
					"IP_DIA integer,"+
					"IP_DPN integer,"+
					"IP_TTL integer,"+
					"IP_NHA integer,"+
					"DSR_NONT integer,"+
					"DSR_RReqF integer,"+
					"DSR_RReqSN integer,"+
					"DSR_RRepF integer,"+
					"DSR_RRepSN integer,"+
					"DSR_RL integer,"+
					"DSR_SOSR integer,"+
					"DSR_DOSR integer,"+
					"DSR_ERF integer,"+
					"DSR_NOE integer,"+
					"DSR_RTW integer,"+
					"DSR_LEF integer,"+
					"DSR_LET integer,"+
					"AODV_ReqType integer,"+
					"AODV_ReqHC integer,"+
					"AODV_ReqBI integer,"+
					"AODV_ReqDestination integer,"+
					"AODV_ReqDSN integer,"+
					"AODV_ReqSource integer,"+
					"AODV_ReqSSN integer,"+
					"AODV_Type integer,"+
					"AODV_HC integer,"+
					"AODV_Destination integer,"+
					"AODV_DSN integer,"+
					"AODV_Lifetime integer,"+
					"AODV_Operation varchar(5),"+
					"TORA_QueType integer,"+
					"TORA_QueDestination integer,"+
					"TORA_UpdType integer,"+
					"TORA_UpdDestination integer,"+
					"TORA_UpdTau double,"+
					"TORA_UpdOid integer,"+
					"TORA_UpdR integer,"+
					"TORA_UpdDelta integer,"+
					"TORA_UpdID integer,"+
					"TORA_CleType integer,"+
					"TORA_CleDestination integer,"+
					"TORA_CleTau double,"+
					"TORA_CleOid integer,"+
					"TCP_SN integer,"+
					"TCP_AN integer,"+
					"TCP_NOTPWF integer,"+
					"TCP_ONOF integer,"+
					"CBR_SN integer,"+
					"CBR_NOTPWF integer,"+
					"CBR_ONOF integer,"+
					"IMEP_AF char(1),"+
					"IMEP_HF char(1),"+
					"IMEP_OF char(1),"+
					"IMEP_Length integer,"+
					"Name varchar(4)"+")");			
		
//			stm1.execute("drop table "+tablePrefix+"old_wireless_tr");			
		/*
		 * New_wireless_tr	
    Event 	Event 	Char(1)
	Time	Time	Double
	Ni	Node id	Integer
	Nx	Node x coordinate	Double
	Ny	Node y coordinate	Double
	Nz	Node z coordinate	Double
	Ne	Node energy level 	Doubel
	Nl	Network trace level	Varchar(3)
	Nw	Drop reason	Varchar(7)
	Hs	Hop source node ID	Integer
	Hd	Hop destination node ID	Integer
	Ma	Duration	Integer
	Ms	Source ethernet address	Integer
	Md	Destination ethernet address	Integer
	Mt 	Ethernet type	Integer
	P	Packet type 	Varchar(7)
	Pn	Packet type	Varchar(3)
	ARP_Po	Request or reply 	Varchar(7)
	ARP_Pms	Source mac address	Integer
	ARP_Ps	Source address	Integer
	ARP_Pmd	Destination mac address	Integer
	ARP_Pd	Destination address	Integer
	IP_Isn	Source address node 	Integer
	IP_Isp	Source address port 	Integer
	IP_Idn	Destination address node 	Integer
	IP_Idp	Destination address port	Integer
	IP_It	Packet type 	Varchar(7)
	IP_Il	Packet size	Integer
	IP_If	Flow id	Integer
	IP_Ii	Unique id 	Integer
	IP_Iv	TTL value 	Integer
	DSR_Ph	Number of node traversed	Integer
	DSR_Pq	Routing request flag	Integer
	DSR_Ps	Route request sequence number	Integer
	DSR_Pp	Routing reply flag	Integer
	DSR_Pn	Route request sequence number 	Integer
	DSR_Pl	Reply length 	Integer
	DSR_Pes	Source 	Integer
	DSR_Ped	Destination	Integer
	DSR_Pw	Error report flag	Integer
	DSR_Pm	Number of errors	integer
	DSR_Pc	Report to whom 	Integer
	DSR_Pba	Link error from a	Integer
	DSR_Pbb	Link error to b	Integer
	AODV_Pt	Type	Integer
	AODV_Ph	Hop count	Integer
	AODV_Pb 	Broadcast id	Integer
	AODV_Pd	Destination	Integer
	AODV_Pds	Destination sequence number	Integer
	AODV_Ps	Source	Integer
	AODV_Pss	Source sequence number	Integer
	AODV_Pl	Lifetime	Double
	AODV_Pc	Operation	Varchar(7)
	TORA_Pt	Type	Integer
	TORA_Pd	Destination	Integer
	TORA_Pa	Time	Double
	TORA_Po	Creator id	Integer
	TORA_Pr	R	Integer
	TORA_Pe	Delta	Intger
	TORA_Pi	Id	Integer
	TORA_Pc	Operation	Varchar(6)
	TCP_Ps	Sequence number	Integer
	TCP_Pa	Acknowledgment number 	Integer
	TCP_Pf	Number of times packet was forwarded	Integer
	TCP_Po	Optimal number of forwards	Integer
	CBR_Pi	Sequence number	Integer
	CBR_Pf	Number of times packet was forwarded	integer
	CBR_Po	Optimal number of forwards	Integer
	IMEP_Pa	Acknowledgment flag	Char(1)
	IMEP_Ph	Hello flag	Char(1)
	IMEP_Po	Object flag	Char(1)
	IMEP_Pl	Length	Integer

		 * 
		 */	
					
			
			//create tables for new wireless trace
			stm1.execute("create table "+tablePrefix+"new_wireless_tr ("+"Event varchar(1),"+
					"Time double,"+
					"Ni integer,"+
					"Nx double,"+
					"Ny double,"+
					"Nz double,"+
					"Ne double,"+
					"Nl varchar(3),"+
					"Nw varchar(3),"+
					"Hs integer,"+
					"Hd integer,"+
					"Ma integer,"+
					"Ms integer,"+
					"Md integer,"+
					"Mt integer,"+
					"P varchar(6),"+
					"Pn varchar(6),"+
					"ARP_Po varchar(7),"+
					"ARP_Pms integer,"+
					"ARP_Ps integer,"+
					"ARP_Pmd integer,"+
					"ARP_Pd integer,"+
					"IP_Isn integer,"+
					"IP_Isp integer,"+
					"IP_Idn integer,"+
					"IP_Idp integer,"+
					"IP_It varchar(7),"+
					"IP_Il integer,"+
					"IP_If integer,"+
					"IP_Ii integer,"+
					"IP_Iv integer,"+
					"DSR_Ph integer,"+
					"DSR_Pq integer,"+
					"DSR_Ps integer,"+
					"DSR_Pp integer,"+
					"DSR_Pn integer,"+
					"DSR_Pl integer,"+
					"DSR_Pes integer,"+
					"DSR_Ped integer,"+
					"DSR_Pw integer,"+
					"DSR_Pm integer,"+
					"DSR_Pc integer,"+
					"DSR_Pba integer,"+
					"DSR_Pbb integer,"+
					"AODV_Pt integer,"+
					"AODV_Ph integer,"+
					"AODV_Pb integer,"+
					"AODV_Pd integer,"+
					"AODV_Pds integer,"+
					"AODV_Ps integer,"+
					"AODV_Pss integer,"+
					"AODV_Pl double,"+
					"AODV_Pc varchar(7),"+
					"TORA_Pt integer,"+
					"TORA_Pd integer,"+
					"TORA_Pa double,"+
					"TORA_Po integer,"+
					"TORA_Pr integer,"+
					"TORA_Pe integer,"+
					"TORA_Pi integer,"+
					"TORA_Pc varchar(6),"+
					"TCP_Ps integer,"+
					"TCP_Pa integer,"+
					"TCP_Pf integer,"+
					"TCP_Po integer,"+
					"CBR_Pi integer,"+
					"CBR_Pf integer,"+
					"CBR_Po integer,"+
					"IMEP_Pa char(1),"+
					"IMEP_Ph char(1),"+
					"IMEP_Po char(1),"+
					"IMEP_Pl integer,"+
					"Name varchar(4)"+")");
//			stm1.execute("drop table "+tablePrefix+"new_wireless_tr");			
			
			
			//create  a table for movement trace
			stm1.execute("create table "+tablePrefix+"movement_tr ("+"Event varchar(1),"+
					"Time double,"+
					"NodeID integer,"+
					"X double,"+
					"Y double,"+
					"Z double,"+
					"DestinationX double,"+
					"DestinationY double,"+
					"Speed double"+")");			
//			stm1.execute("drop table "+tablePrefix+"movement_tr");
			
//			create a table for energy trace
//          this is used for old wireless trace
			stm1.execute("create table "+tablePrefix+"energy_tr ("+"Event varchar(1),"+
					"t double,"+
					"n integer,"+
					"e double"+")");			
//			stm1.execute("drop table "+tablePrefix+"energy_tr");
			
//			con.close();
			
		} catch (SQLException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}
	}

}
