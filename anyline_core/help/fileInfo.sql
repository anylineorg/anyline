
CREATE TABLE [dbo].[UPLOAD_FILE](
  [CD] [varchar](50) NOT NULL,
  [CODE] [varchar](50) NULL,
  [TITLE] [varchar](50) NULL,
  [SRC_NAME] [varchar](50) NULL,
  [ROOT] [varchar](500) NULL,
  [PATH_REL] [varchar](500) NULL,
  [PATH_ABS] [varchar](500) NULL,
  [HEIGHT] [int] NULL,
  [WIDTH] [int] NULL,
  [DESCRIPTION] [nvarchar](1000) NULL,
  [IDX] [nvarchar](10) NULL,
  [REMARK] [nvarchar](500) NULL,
  [REG_CD] [varchar](50) NULL,
  [REG_IP] [varchar](20) NULL,
  [REG_TIME] [datetime] NULL,
  [REG_CLIENT_CD] [varchar](50) NULL,
  [UPT_CD] [varchar](50) NULL,
  [UPT_IP] [varchar](20) NULL,
  [UPT_TIME] [datetime] NULL,
  [UPT_CLIENT_CD] [varchar](50) NULL,
  [STATUS] [int] NULL,
 CONSTRAINT [PK_FILE_INFO] PRIMARY KEY CLUSTERED 
(
  [CD] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
