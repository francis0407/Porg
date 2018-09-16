class job:
    def __init__(self, name, cid, job_dir, program,
    input_url, map_num, reduce_num):
        self.name = name
        self.cid = cid
        self.job_dir = job_dir
        self.program = program
        self.input_url = input_url
        self.map_num = map_num
        self.reduce_num = reduce_num
